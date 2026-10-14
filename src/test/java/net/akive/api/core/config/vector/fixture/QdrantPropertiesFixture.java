package net.akive.api.core.config.vector.fixture;

import net.akive.api.core.config.vector.QdrantProperties;

import java.time.Duration;

public final class QdrantPropertiesFixture {

    public static final String QDRANT_HOST = "localhost";
    public static final int QDRANT_GRPC_PORT = 6334;
    public static final boolean QDRANT_TLS_ENABLED = false;
    public static final Duration QDRANT_TIMEOUT = Duration.ofSeconds(10);
    public static final String LEARNER_INSIGHT_COLLECTION_NAME = "learner_insight";

    private QdrantPropertiesFixture() {
    }

    public static QdrantProperties createQdrantProperties() {
        return new QdrantProperties(
                QDRANT_HOST,
                QDRANT_GRPC_PORT,
                QDRANT_TLS_ENABLED,
                QDRANT_TIMEOUT,
                LEARNER_INSIGHT_COLLECTION_NAME
        );
    }
}
