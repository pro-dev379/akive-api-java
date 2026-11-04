package net.akive.api.core.config.vector;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.grpc.v1_6.GrpcTelemetry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QdrantConfig {

    @Bean(destroyMethod = "close")
    public QdrantClient qdrantClient(QdrantProperties qdrantProperties, OpenTelemetry openTelemetry) {
        ManagedChannel channel = createChannel(qdrantProperties, openTelemetry);
        QdrantGrpcClient grpcClient = QdrantGrpcClient.newBuilder(channel, true).build();

        return new QdrantClient(grpcClient);
    }

    private ManagedChannel createChannel(QdrantProperties qdrantProperties, OpenTelemetry openTelemetry) {
        ManagedChannelBuilder<?> channelBuilder = ManagedChannelBuilder.forAddress(
                qdrantProperties.host(),
                qdrantProperties.grpcPort()
        );

        if (qdrantProperties.tlsEnabled()) {
            channelBuilder.useTransportSecurity();
        } else {
            channelBuilder.usePlaintext();
        }

        return channelBuilder
                .intercept(GrpcTelemetry.create(openTelemetry).newClientInterceptor())
                .build();
    }
}
