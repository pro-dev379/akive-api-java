package net.akive.api.learner.adapter.vector;

import com.google.common.util.concurrent.ListenableFuture;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.Collections.CollectionInfo;
import io.qdrant.client.grpc.Collections.Distance;
import io.qdrant.client.grpc.Collections.PayloadSchemaType;
import io.qdrant.client.grpc.Collections.VectorParams;
import io.qdrant.client.grpc.Collections.VectorsConfig;
import io.qdrant.client.grpc.Common.Filter;
import io.qdrant.client.grpc.Common.PointId;
import io.qdrant.client.grpc.JsonWithInt.Value;
import io.qdrant.client.grpc.Points.DenseVector;
import io.qdrant.client.grpc.Points.PointStruct;
import io.qdrant.client.grpc.Points.Query;
import io.qdrant.client.grpc.Points.QueryPoints;
import io.qdrant.client.grpc.Points.ScoredPoint;
import io.qdrant.client.grpc.Points.VectorInput;
import lombok.RequiredArgsConstructor;
import net.akive.api.core.config.llm.OpenAiProperties;
import net.akive.api.core.config.vector.QdrantProperties;
import net.akive.api.learner.application.exception.LearnerInsightEmbeddingException;
import net.akive.api.learner.application.required.LearnerInsightVectorStore;
import net.akive.api.learner.application.required.data.LearnerInsightVectorData;
import net.akive.api.learner.domain.InsightStatus;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static io.qdrant.client.ConditionFactory.match;
import static io.qdrant.client.ConditionFactory.matchKeywords;
import static io.qdrant.client.PointIdFactory.id;
import static io.qdrant.client.ValueFactory.value;
import static io.qdrant.client.VectorsFactory.vectors;
import static io.qdrant.client.grpc.Collections.PayloadSchemaType.Integer;
import static io.qdrant.client.grpc.Collections.PayloadSchemaType.Keyword;

@Component
@RequiredArgsConstructor
class QdrantLearnerInsightVectorStore implements LearnerInsightVectorStore {

    private static final String USER_ID_PAYLOAD_KEY = "userId";
    private static final String STATUS_PAYLOAD_KEY = "status";

    private final QdrantClient qdrantClient;
    private final QdrantProperties qdrantProperties;
    private final OpenAiProperties openAiProperties;

    private volatile boolean collectionReady;

    @Override
    public void save(LearnerInsightVectorData learnerInsight, List<Float> embeddingValues) {
        validateEmbeddingDimension(embeddingValues);
        ensureCollection();

        Map<String, Value> payload = Map.of(
                USER_ID_PAYLOAD_KEY, value(learnerInsight.userId()),
                STATUS_PAYLOAD_KEY, value(learnerInsight.status().name())
        );
        PointStruct point = PointStruct.newBuilder()
                .setId(id(learnerInsight.learnerInsightId()))
                .setVectors(vectors(embeddingValues))
                .putAllPayload(payload)
                .build();

        await(
                qdrantClient.upsertAsync(
                        qdrantProperties.learnerInsightCollectionName(),
                        List.of(point),
                        qdrantProperties.timeout()
                ),
                "save a learner insight vector: " + learnerInsight.learnerInsightId()
        );
    }

    @Override
    public List<Long> findSimilarIds(
            Long userId,
            Collection<InsightStatus> statuses,
            List<Float> queryEmbeddingValues,
            int limit
    ) {
        validateEmbeddingDimension(queryEmbeddingValues);
        ensureCollection();

        Filter filter = Filter.newBuilder()
                .addMust(match(USER_ID_PAYLOAD_KEY, userId))
                .addMust(matchKeywords(STATUS_PAYLOAD_KEY, statuses.stream().map(Enum::name).toList()))
                .build();
        VectorInput vectorInput = VectorInput.newBuilder()
                .setDense(DenseVector.newBuilder().addAllData(queryEmbeddingValues).build())
                .build();
        QueryPoints query = QueryPoints.newBuilder()
                .setCollectionName(qdrantProperties.learnerInsightCollectionName())
                .setQuery(Query.newBuilder().setNearest(vectorInput).build())
                .setFilter(filter)
                .setLimit(limit)
                .build();

        return await(qdrantClient.queryAsync(query, qdrantProperties.timeout()), "search learner insight vectors")
                .stream()
                .map(ScoredPoint::getId)
                .map(this::extractNumericPointId)
                .toList();
    }

    private synchronized void ensureCollection() {
        if (this.collectionReady) {
            return;
        }

        String collectionName = qdrantProperties.learnerInsightCollectionName();

        if (!await(
                qdrantClient.collectionExistsAsync(collectionName, qdrantProperties.timeout()),
                "check the learner insight collection"
        )) {
            createCollection(collectionName);
        }

        CollectionInfo collectionInfo = await(
                qdrantClient.getCollectionInfoAsync(collectionName, qdrantProperties.timeout()),
                "read the learner insight collection"
        );

        validateCollection(collectionInfo);
        createPayloadIndexIfAbsent(collectionInfo, USER_ID_PAYLOAD_KEY, Integer);
        createPayloadIndexIfAbsent(collectionInfo, STATUS_PAYLOAD_KEY, Keyword);

        this.collectionReady = true;
    }

    private void createCollection(String collectionName) {
        VectorParams vectorParams = VectorParams.newBuilder()
                .setSize(openAiProperties.embedding().dimensions())
                .setDistance(Distance.Cosine)
                .build();

        try {
            await(
                    qdrantClient.createCollectionAsync(
                            collectionName,
                            vectorParams,
                            qdrantProperties.timeout()
                    ),
                    "create the learner insight collection"
            );
        } catch (LearnerInsightEmbeddingException e) {
            boolean collectionExists = await(
                    qdrantClient.collectionExistsAsync(collectionName, qdrantProperties.timeout()),
                    "recheck the learner insight collection"
            );

            if (!collectionExists) {
                throw e;
            }
        }
    }

    private void createPayloadIndexIfAbsent(
            CollectionInfo collectionInfo,
            String fieldName,
            PayloadSchemaType fieldType
    ) {
        if (collectionInfo.getPayloadSchemaMap().containsKey(fieldName)) {
            return;
        }

        try {
            await(
                    qdrantClient.createPayloadIndexAsync(
                            qdrantProperties.learnerInsightCollectionName(),
                            fieldName,
                            fieldType,
                            null,
                            true,
                            null,
                            qdrantProperties.timeout()
                    ),
                    "create a learner insight payload index: " + fieldName
            );
        } catch (LearnerInsightEmbeddingException e) {
            CollectionInfo refreshedCollectionInfo = await(
                    qdrantClient.getCollectionInfoAsync(
                            qdrantProperties.learnerInsightCollectionName(),
                            qdrantProperties.timeout()
                    ),
                    "recheck a learner insight payload index: " + fieldName
            );

            if (!refreshedCollectionInfo.getPayloadSchemaMap().containsKey(fieldName)) {
                throw e;
            }
        }
    }

    private void validateCollection(CollectionInfo collectionInfo) {
        VectorsConfig vectorsConfig = collectionInfo.getConfig()
                .getParams()
                .getVectorsConfig();

        if (vectorsConfig.getConfigCase() != VectorsConfig.ConfigCase.PARAMS) {
            throw new LearnerInsightEmbeddingException(
                    "Learner insight collection does not use an unnamed dense vector."
            );
        }

        VectorParams vectorParams = vectorsConfig.getParams();

        if (vectorParams.getSize() != openAiProperties.embedding().dimensions()
                || vectorParams.getDistance() != Distance.Cosine) {
            throw new LearnerInsightEmbeddingException(
                    "Learner insight collection configuration does not match: dimensions="
                            + vectorParams.getSize() + ", distance=" + vectorParams.getDistance()
            );
        }
    }

    private void validateEmbeddingDimension(List<Float> embedding) {
        if (embedding == null || embedding.size() != openAiProperties.embedding().dimensions()) {
            throw new LearnerInsightEmbeddingException(
                    "Invalid learner insight embedding dimension: "
                            + (embedding == null ? "null" : embedding.size())
            );
        }
    }

    private Long extractNumericPointId(PointId pointId) {
        if (pointId.getPointIdOptionsCase() != PointId.PointIdOptionsCase.NUM) {
            throw new LearnerInsightEmbeddingException("Qdrant returned a non-numeric learner insight point ID.");
        }

        return pointId.getNum();
    }

    private <T> T await(ListenableFuture<T> future, String operation) {
        try {
            return future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LearnerInsightEmbeddingException("Interrupted while attempting to " + operation + ".", e);
        } catch (ExecutionException e) {
            throw new LearnerInsightEmbeddingException("Failed to " + operation + ".", e.getCause());
        }
    }
}
