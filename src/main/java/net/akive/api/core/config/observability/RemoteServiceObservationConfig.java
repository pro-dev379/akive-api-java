package net.akive.api.core.config.observability;

import io.awspring.cloud.sqs.support.observation.SqsListenerObservation;
import io.awspring.cloud.sqs.support.observation.SqsTemplateObservation;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationFilter;
import net.ttddyy.observation.tracing.DataSourceBaseContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RemoteServiceObservationConfig {

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
            dataSourceContext.setRemoteServiceName(remoteServiceNamingProperties.database());
        } else if (context instanceof SqsTemplateObservation.Context sqsTemplateContext) {
            sqsTemplateContext.setRemoteServiceName(sqsTemplateContext.getDestinationName());
        } else if (context instanceof SqsListenerObservation.Context sqsListenerContext) {
            sqsListenerContext.setRemoteServiceName(sqsListenerContext.getSourceName());
        }

        return context;
    }
}
