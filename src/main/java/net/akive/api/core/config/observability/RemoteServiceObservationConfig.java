package net.akive.api.core.config.observability;

import io.awspring.cloud.sqs.support.observation.SqsListenerObservation;
import io.awspring.cloud.sqs.support.observation.SqsTemplateObservation;
import io.micrometer.common.KeyValue;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationFilter;
import net.ttddyy.observation.tracing.DataSourceBaseContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RemoteServiceObservationConfig {

    private static final String AWS_REMOTE_SERVICE_ATTRIBUTE = "aws.remote.service";

    @Bean
    public ObservationFilter remoteServiceObservationFilter(
            RemoteServiceNamingProperties remoteServiceNamingProperties
    ) {
        return context -> configureRemoteServiceName(context, remoteServiceNamingProperties);
    }

    private Observation.Context configureRemoteServiceName(
            Observation.Context context,
            RemoteServiceNamingProperties remoteServiceNamingProperties
    ) {
        if (context instanceof DataSourceBaseContext dataSourceContext) {
            String databaseName = remoteServiceNamingProperties.database();
            dataSourceContext.setRemoteServiceName(databaseName);
            addAwsRemoteServiceAttribute(dataSourceContext, databaseName);
        } else if (context instanceof SqsTemplateObservation.Context sqsTemplateContext) {
            String destinationName = sqsTemplateContext.getDestinationName();
            sqsTemplateContext.setRemoteServiceName(destinationName);
            addAwsRemoteServiceAttribute(sqsTemplateContext, destinationName);
        } else if (context instanceof SqsListenerObservation.Context sqsListenerContext) {
            String sourceName = sqsListenerContext.getSourceName();
            sqsListenerContext.setRemoteServiceName(sourceName);
            addAwsRemoteServiceAttribute(sqsListenerContext, sourceName);
        }

        return context;
    }

    private void addAwsRemoteServiceAttribute(Observation.Context context, String remoteServiceName) {
        context.addLowCardinalityKeyValue(KeyValue.of(AWS_REMOTE_SERVICE_ATTRIBUTE, remoteServiceName));
    }
}
