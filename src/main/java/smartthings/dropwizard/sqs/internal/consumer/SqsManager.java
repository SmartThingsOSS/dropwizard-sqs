package smartthings.dropwizard.sqs.internal.consumer;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.dropwizard.lifecycle.Managed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import smartthings.dropwizard.sqs.AmazonSQSProvider;
import smartthings.dropwizard.sqs.DefaultSqsService;
import smartthings.dropwizard.sqs.SqsModule;
import smartthings.dropwizard.sqs.SqsService;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class SqsManager implements Managed {

    private static final Logger LOG = LoggerFactory.getLogger(SqsManager.class);

    private final Map<String, SqsService> sqsMap = new ConcurrentHashMap<>();
    private final SqsModule.Config config;
    private final AmazonSQSProvider sqsProvider;

    @Inject
    public SqsManager(SqsModule.Config config, AmazonSQSProvider sqsProvider) {
        this.config = config;
        this.sqsProvider = sqsProvider;
    }

    @Override
    public void start() {
        if (config.isEnabled()) {
            LOG.debug("Starting up SqsManager...");
            config.getConsumers().stream()
                .filter(SqsModule.ConsumerConfig::isEnabled)
                .map(SqsModule.ConsumerConfig::getEndpoints)
                .flatMap(Collection::stream)
                .forEach(this::create);
        } else {
            LOG.debug("Skipping start up of SqsManager...");
        }
    }

    @Override
    public void stop() {
        LOG.debug("Shutting down SqsManager...");
    }

    public SqsService get(SqsModule.EndpointConfig config) {
        SqsService sqs = sqsMap.get(getCacheKey(config));
        if (sqs == null) {
            LOG.error(
                "No SQS client exists for region={} endpoint={}",
                config.getRegionName(), config.endpoint().orElse("none")
            );
            throw new IllegalStateException("Unable to resolve SQS client for Endpoint");
        }
        return sqs;
    }

    private SqsService create(SqsModule.EndpointConfig config) {
        if (config.getRegionName() == null) {
            throw new IllegalArgumentException("Consumer endpoint config requires a valid configured AWS Region.");
        }
        String cacheKey = getCacheKey(config);
        LOG.debug("Creating SqsService for endpoint={}", cacheKey);
        if (sqsMap.containsKey(cacheKey)) {
            return sqsMap.get(cacheKey);
        }
        SqsService sqsService = new DefaultSqsService(sqsProvider.get(config));
        sqsMap.put(cacheKey, sqsService);
        return sqsService;
    }

    private String getCacheKey(SqsModule.EndpointConfig config) {
        return config.getRegionName() + ":" + config.endpoint().orElse("none");
    }

}
