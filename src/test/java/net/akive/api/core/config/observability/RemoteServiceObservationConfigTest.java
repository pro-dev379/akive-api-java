package net.akive.api.core.config.observability;

import io.awspring.cloud.sqs.listener.SqsHeaders;
import io.awspring.cloud.sqs.support.observation.SqsListenerObservation;
import io.awspring.cloud.sqs.support.observation.SqsTemplateObservation;
import io.micrometer.observation.ObservationFilter;
import net.ttddyy.observation.tracing.QueryContext;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import static org.assertj.core.api.Assertions.assertThat;

class RemoteServiceObservationConfigTest {

    private static final String DATABASE_SERVICE_NAME = "akive-rds-dev";
    private static final String REDIS_SERVICE_NAME = "akive-redis-dev";
    private static final String QDRANT_SERVICE_NAME = "akive-qdrant-dev";
    private static final String QUEUE_NAME = "akive-produced-event-dev.fifo";

    private final ObservationFilter remoteServiceObservationFilter = new RemoteServiceObservationConfig()
            .remoteServiceObservationFilter(new RemoteServiceNamingProperties(
                    DATABASE_SERVICE_NAME,
                    REDIS_SERVICE_NAME,
                    QDRANT_SERVICE_NAME
            ));

    @Test
    void 데이터베이스_추적_대상_이름을_설정한다() {
        QueryContext queryContext = new QueryContext();

        remoteServiceObservationFilter.map(queryContext);

        assertThat(queryContext.getRemoteServiceName()).isEqualTo(DATABASE_SERVICE_NAME);
    }

    @Test
    void SQS_메시지_발행_추적_대상_이름을_설정한다() {
        Message<String> message = MessageBuilder.withPayload("payload").build();
        SqsTemplateObservation.Context context = new SqsTemplateObservation.Context(message, QUEUE_NAME);

        remoteServiceObservationFilter.map(context);

        assertThat(context.getRemoteServiceName()).isEqualTo(QUEUE_NAME);
    }

    @Test
    void SQS_메시지_수신_추적_대상_이름을_설정한다() {
        Message<String> message = MessageBuilder.withPayload("payload")
                .setHeader(SqsHeaders.SQS_QUEUE_NAME_HEADER, QUEUE_NAME)
                .build();
        SqsListenerObservation.Context context = new SqsListenerObservation.Context(message);

        remoteServiceObservationFilter.map(context);

        assertThat(context.getRemoteServiceName()).isEqualTo(QUEUE_NAME);
    }
}
