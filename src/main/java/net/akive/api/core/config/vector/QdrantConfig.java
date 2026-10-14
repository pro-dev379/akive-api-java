package net.akive.api.core.config.vector;

import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QdrantConfig {

    @Bean(destroyMethod = "close")
    public QdrantClient qdrantClient(QdrantProperties qdrantProperties) {
        return new QdrantClient(QdrantGrpcClient.newBuilder(
                qdrantProperties.host(),
                qdrantProperties.grpcPort(),
                qdrantProperties.tlsEnabled()
        ).build());
    }
}
