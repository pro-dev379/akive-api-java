package net.akive.api.learner.adapter.vector;

import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.Collections.*;
import io.qdrant.client.grpc.Common.Filter;
import io.qdrant.client.grpc.Common.PointId;
import io.qdrant.client.grpc.JsonWithInt.Value;
import io.qdrant.client.grpc.Points.*;
import net.akive.api.learner.application.exception.LearnerInsightEmbeddingException;
import net.akive.api.learner.application.required.data.LearnerInsightVectorData;
import net.akive.api.learner.domain.InsightStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.google.common.util.concurrent.Futures.immediateFailedFuture;
import static com.google.common.util.concurrent.Futures.immediateFuture;
import static io.qdrant.client.ConditionFactory.match;
import static io.qdrant.client.ConditionFactory.matchKeywords;
import static io.qdrant.client.PointIdFactory.id;
import static io.qdrant.client.ValueFactory.value;
import static io.qdrant.client.VectorsFactory.vectors;
import static io.qdrant.client.grpc.Collections.Distance.Cosine;
import static io.qdrant.client.grpc.Collections.Distance.Dot;
import static io.qdrant.client.grpc.Collections.PayloadSchemaType.Integer;
import static io.qdrant.client.grpc.Collections.PayloadSchemaType.Keyword;
import static java.util.Collections.nCopies;
import static net.akive.api.core.config.llm.fixture.OpenAiPropertiesFixture.EMBEDDING_DIMENSIONS;
import static net.akive.api.core.config.llm.fixture.OpenAiPropertiesFixture.createOpenAiProperties;
import static net.akive.api.core.config.vector.fixture.QdrantPropertiesFixture.*;
import static net.akive.api.learner.application.fixture.LearnerInsightQueryFixture.FILTER_STATUSES;
import static net.akive.api.learner.application.fixture.LearnerInsightQueryFixture.SEARCH_RESULT_MAX_COUNT;
import static net.akive.api.learner.application.fixture.TextEmbeddingFixture.EMBEDDING_VALUES;
import static net.akive.api.learner.domain.InsightStatus.ACTIVE;
import static net.akive.api.learner.domain.InsightStatus.CANDIDATE;
import static net.akive.api.learner.domain.fixture.LearnerInsightFixture.ANOTHER_LEARNER_INSIGHT_ID;
import static net.akive.api.learner.domain.fixture.LearnerInsightFixture.LEARNER_INSIGHT_ID;
import static net.akive.api.user.domain.fixture.UserFixture.USER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class QdrantLearnerInsightVectorStoreTest {

    private static final String USER_ID_PAYLOAD_KEY = "userId";
    private static final String STATUS_PAYLOAD_KEY = "status";

    @Mock
    QdrantClient qdrantClient;

    QdrantLearnerInsightVectorStore qdrantLearnerInsightVectorStore;

    @BeforeEach
    void setUp() {
        qdrantLearnerInsightVectorStore = new QdrantLearnerInsightVectorStore(
                qdrantClient,
                createQdrantProperties(),
                createOpenAiProperties()
        );
    }

    @Test
    void 학습자_인사이트의_임베딩_벡터와_검색용_페이로드를_Qdrant에_저장한다() {
        LearnerInsightVectorData learnerInsight = new LearnerInsightVectorData(
                LEARNER_INSIGHT_ID,
                USER_ID,
                ACTIVE
        );
        prepareExistingCollection();
        given(qdrantClient.upsertAsync(
                LEARNER_INSIGHT_COLLECTION_NAME,
                List.of(createPoint(learnerInsight)),
                QDRANT_TIMEOUT
        )).willReturn(immediateFuture(UpdateResult.getDefaultInstance()));

        qdrantLearnerInsightVectorStore.save(learnerInsight, EMBEDDING_VALUES);

        then(qdrantClient).should().upsertAsync(
                LEARNER_INSIGHT_COLLECTION_NAME,
                List.of(createPoint(learnerInsight)),
                QDRANT_TIMEOUT
        );
    }

    @Test
    void 회원_식별자와_학습자_인사이트_상태가_일치하는_유사한_학습자_인사이트_식별자를_조회한다() {
        QueryPoints query = createQuery(USER_ID, FILTER_STATUSES);
        List<ScoredPoint> scoredPoints = List.of(
                createScoredPoint(LEARNER_INSIGHT_ID),
                createScoredPoint(ANOTHER_LEARNER_INSIGHT_ID)
        );
        prepareExistingCollection();
        given(qdrantClient.queryAsync(query, QDRANT_TIMEOUT))
                .willReturn(immediateFuture(scoredPoints));

        List<Long> learnerInsightIds = qdrantLearnerInsightVectorStore.findSimilarIds(
                USER_ID,
                FILTER_STATUSES,
                EMBEDDING_VALUES,
                SEARCH_RESULT_MAX_COUNT
        );

        assertThat(learnerInsightIds).containsExactly(LEARNER_INSIGHT_ID, ANOTHER_LEARNER_INSIGHT_ID);
        then(qdrantClient).should().queryAsync(query, QDRANT_TIMEOUT);
    }

    @Test
    void Qdrant_컬렉션이_없으면_설정한_임베딩_차원과_코사인_유사도를_적용해_생성한다() {
        LearnerInsightVectorData learnerInsight = new LearnerInsightVectorData(
                LEARNER_INSIGHT_ID,
                USER_ID,
                CANDIDATE
        );
        VectorParams vectorParams = createVectorParams(EMBEDDING_DIMENSIONS, Cosine);
        given(qdrantClient.collectionExistsAsync(LEARNER_INSIGHT_COLLECTION_NAME, QDRANT_TIMEOUT))
                .willReturn(immediateFuture(false));
        given(qdrantClient.createCollectionAsync(
                LEARNER_INSIGHT_COLLECTION_NAME,
                vectorParams,
                QDRANT_TIMEOUT
        )).willReturn(immediateFuture(CollectionOperationResponse.getDefaultInstance()));
        given(qdrantClient.getCollectionInfoAsync(LEARNER_INSIGHT_COLLECTION_NAME, QDRANT_TIMEOUT))
                .willReturn(immediateFuture(createCollectionInfo(vectorParams, true)));
        given(qdrantClient.upsertAsync(
                LEARNER_INSIGHT_COLLECTION_NAME,
                List.of(createPoint(learnerInsight)),
                QDRANT_TIMEOUT
        )).willReturn(immediateFuture(UpdateResult.getDefaultInstance()));

        qdrantLearnerInsightVectorStore.save(learnerInsight, EMBEDDING_VALUES);

        then(qdrantClient).should().createCollectionAsync(
                LEARNER_INSIGHT_COLLECTION_NAME,
                vectorParams,
                QDRANT_TIMEOUT
        );
    }

    @Test
    void Qdrant_컬렉션에_회원_식별자와_학습자_인사이트_상태를_위한_페이로드_인덱스가_없으면_각_인덱스를_생성한다() {
        LearnerInsightVectorData learnerInsight = new LearnerInsightVectorData(
                LEARNER_INSIGHT_ID,
                USER_ID,
                CANDIDATE
        );
        VectorParams vectorParams = createVectorParams(EMBEDDING_DIMENSIONS, Cosine);
        given(qdrantClient.collectionExistsAsync(LEARNER_INSIGHT_COLLECTION_NAME, QDRANT_TIMEOUT))
                .willReturn(immediateFuture(true));
        given(qdrantClient.getCollectionInfoAsync(LEARNER_INSIGHT_COLLECTION_NAME, QDRANT_TIMEOUT))
                .willReturn(immediateFuture(createCollectionInfo(vectorParams, false)));
        given(qdrantClient.createPayloadIndexAsync(
                LEARNER_INSIGHT_COLLECTION_NAME,
                USER_ID_PAYLOAD_KEY,
                Integer,
                null,
                true,
                null,
                QDRANT_TIMEOUT
        )).willReturn(immediateFuture(UpdateResult.getDefaultInstance()));
        given(qdrantClient.createPayloadIndexAsync(
                LEARNER_INSIGHT_COLLECTION_NAME,
                STATUS_PAYLOAD_KEY,
                Keyword,
                null,
                true,
                null,
                QDRANT_TIMEOUT
        )).willReturn(immediateFuture(UpdateResult.getDefaultInstance()));
        given(qdrantClient.upsertAsync(
                LEARNER_INSIGHT_COLLECTION_NAME,
                List.of(createPoint(learnerInsight)),
                QDRANT_TIMEOUT
        )).willReturn(immediateFuture(UpdateResult.getDefaultInstance()));

        qdrantLearnerInsightVectorStore.save(learnerInsight, EMBEDDING_VALUES);

        then(qdrantClient).should().createPayloadIndexAsync(
                LEARNER_INSIGHT_COLLECTION_NAME,
                USER_ID_PAYLOAD_KEY,
                Integer,
                null,
                true,
                null,
                QDRANT_TIMEOUT
        );
        then(qdrantClient).should().createPayloadIndexAsync(
                LEARNER_INSIGHT_COLLECTION_NAME,
                STATUS_PAYLOAD_KEY,
                Keyword,
                null,
                true,
                null,
                QDRANT_TIMEOUT
        );
    }

    @Test
    void 저장할_임베딩_벡터의_차원이_설정한_차원과_다르면_예외를_던진다() {
        LearnerInsightVectorData learnerInsight = new LearnerInsightVectorData(
                LEARNER_INSIGHT_ID,
                USER_ID,
                ACTIVE
        );
        List<Float> unexpectedDimensionEmbeddingValues = nCopies(EMBEDDING_DIMENSIONS - 1, 0.1F);

        assertThatExceptionOfType(LearnerInsightEmbeddingException.class)
                .isThrownBy(() -> qdrantLearnerInsightVectorStore.save(
                        learnerInsight,
                        unexpectedDimensionEmbeddingValues
                ))
                .withMessage("Invalid learner insight embedding dimension: "
                        + unexpectedDimensionEmbeddingValues.size());

        then(qdrantClient).shouldHaveNoInteractions();
    }

    @Test
    void Qdrant_컬렉션의_벡터_차원이나_유사도_계산_방식이_애플리케이션_설정과_다르면_예외를_던진다() {
        LearnerInsightVectorData learnerInsight = new LearnerInsightVectorData(
                LEARNER_INSIGHT_ID,
                USER_ID,
                ACTIVE
        );
        VectorParams unexpectedVectorParams = createVectorParams(EMBEDDING_DIMENSIONS - 1, Dot);
        given(qdrantClient.collectionExistsAsync(LEARNER_INSIGHT_COLLECTION_NAME, QDRANT_TIMEOUT))
                .willReturn(immediateFuture(true));
        given(qdrantClient.getCollectionInfoAsync(LEARNER_INSIGHT_COLLECTION_NAME, QDRANT_TIMEOUT))
                .willReturn(immediateFuture(createCollectionInfo(unexpectedVectorParams, true)));

        assertThatExceptionOfType(LearnerInsightEmbeddingException.class)
                .isThrownBy(() -> qdrantLearnerInsightVectorStore.save(learnerInsight, EMBEDDING_VALUES))
                .withMessage("Learner insight collection configuration does not match: dimensions="
                        + (EMBEDDING_DIMENSIONS - 1) + ", distance=Dot");
    }

    @Test
    void Qdrant가_UUID_형식의_포인트_식별자를_반환하면_예외를_던진다() {
        List<InsightStatus> statuses = List.of(ACTIVE);
        QueryPoints query = createQuery(USER_ID, statuses);
        prepareExistingCollection();
        ScoredPoint scoredPoint = ScoredPoint.newBuilder()
                .setId(PointId.newBuilder().setUuid("0199a8e2-7c31-7abc-8123-456789abcdef"))
                .build();
        given(qdrantClient.queryAsync(query, QDRANT_TIMEOUT))
                .willReturn(immediateFuture(List.of(scoredPoint)));

        assertThatExceptionOfType(LearnerInsightEmbeddingException.class)
                .isThrownBy(() -> qdrantLearnerInsightVectorStore.findSimilarIds(
                        USER_ID,
                        statuses,
                        EMBEDDING_VALUES,
                        SEARCH_RESULT_MAX_COUNT
                ))
                .withMessage("Qdrant returned a non-numeric learner insight point ID.");
    }

    @Test
    void Qdrant_벡터_저장_API_호출에_실패하면_학습자_인사이트_임베딩_예외로_변환해_던진다() {
        LearnerInsightVectorData learnerInsight = new LearnerInsightVectorData(
                LEARNER_INSIGHT_ID,
                USER_ID,
                ACTIVE
        );
        RuntimeException qdrantException = new RuntimeException("Qdrant is unavailable.");
        prepareExistingCollection();
        given(qdrantClient.upsertAsync(
                LEARNER_INSIGHT_COLLECTION_NAME,
                List.of(createPoint(learnerInsight)),
                QDRANT_TIMEOUT
        )).willReturn(immediateFailedFuture(qdrantException));

        assertThatExceptionOfType(LearnerInsightEmbeddingException.class)
                .isThrownBy(() -> qdrantLearnerInsightVectorStore.save(learnerInsight, EMBEDDING_VALUES))
                .withMessage("Failed to save a learner insight vector: " + LEARNER_INSIGHT_ID + ".")
                .withCause(qdrantException);
    }

    private void prepareExistingCollection() {
        VectorParams vectorParams = createVectorParams(EMBEDDING_DIMENSIONS, Cosine);
        given(qdrantClient.collectionExistsAsync(LEARNER_INSIGHT_COLLECTION_NAME, QDRANT_TIMEOUT))
                .willReturn(immediateFuture(true));
        given(qdrantClient.getCollectionInfoAsync(LEARNER_INSIGHT_COLLECTION_NAME, QDRANT_TIMEOUT))
                .willReturn(immediateFuture(createCollectionInfo(vectorParams, true)));
    }

    private CollectionInfo createCollectionInfo(VectorParams vectorParams, boolean withPayloadIndexes) {
        CollectionInfo.Builder collectionInfo = CollectionInfo.newBuilder()
                .setConfig(CollectionConfig.newBuilder()
                        .setParams(CollectionParams.newBuilder()
                                .setVectorsConfig(VectorsConfig.newBuilder().setParams(vectorParams))));

        if (withPayloadIndexes) {
            collectionInfo.putPayloadSchema(USER_ID_PAYLOAD_KEY, PayloadSchemaInfo.getDefaultInstance());
            collectionInfo.putPayloadSchema(STATUS_PAYLOAD_KEY, PayloadSchemaInfo.getDefaultInstance());
        }

        return collectionInfo.build();
    }

    private VectorParams createVectorParams(long dimensions, Distance distance) {
        return VectorParams.newBuilder()
                .setSize(dimensions)
                .setDistance(distance)
                .build();
    }

    private PointStruct createPoint(LearnerInsightVectorData learnerInsight) {
        Map<String, Value> payload = Map.of(
                USER_ID_PAYLOAD_KEY, value(learnerInsight.userId()),
                STATUS_PAYLOAD_KEY, value(learnerInsight.status().name())
        );

        return PointStruct.newBuilder()
                .setId(id(learnerInsight.learnerInsightId()))
                .setVectors(vectors(EMBEDDING_VALUES))
                .putAllPayload(payload)
                .build();
    }

    private QueryPoints createQuery(Long userId, Collection<InsightStatus> statuses) {
        Filter filter = Filter.newBuilder()
                .addMust(match(USER_ID_PAYLOAD_KEY, userId))
                .addMust(matchKeywords(STATUS_PAYLOAD_KEY, statuses.stream().map(Enum::name).toList()))
                .build();
        VectorInput vectorInput = VectorInput.newBuilder()
                .setDense(DenseVector.newBuilder().addAllData(EMBEDDING_VALUES).build())
                .build();

        return QueryPoints.newBuilder()
                .setCollectionName(LEARNER_INSIGHT_COLLECTION_NAME)
                .setQuery(Query.newBuilder().setNearest(vectorInput).build())
                .setFilter(filter)
                .setLimit(SEARCH_RESULT_MAX_COUNT)
                .build();
    }

    private ScoredPoint createScoredPoint(Long learnerInsightId) {
        return ScoredPoint.newBuilder().setId(id(learnerInsightId)).build();
    }
}
