package smartthings.dropwizard.sqs;

import com.google.inject.multibindings.OptionalBinder;
import smartthings.dropwizard.sqs.internal.consumer.ConsumerManager;
import smartthings.dropwizard.sqs.internal.consumer.SqsManager;
import smartthings.dropwizard.sqs.internal.providers.DefaultAmazonSQSProvider;
import smartthings.dw.guice.AbstractDwModule;

import java.util.*;

/**
 * Guice module bindings for the Dropwizard SQS library.
 */
public class SqsModule extends AbstractDwModule {

    @Override
    protected void configure() {
        bind(SqsManager.class).asEagerSingleton();
        bind(ConsumerManager.class).asEagerSingleton();

        OptionalBinder.newOptionalBinder(binder(), AmazonSQSProvider.class)
            .setDefault().to(DefaultAmazonSQSProvider.class);

        registerManaged(SqsManager.class, ConsumerManager.class);
    }

    /**
     * Primary Dropwizard SQS module configuration.
     */
    public static class Config {
        private boolean enabled;
        private List<ConsumerConfig> consumers = Collections.emptyList();
        private Map<String, EndpointConfig> queueWriters = Collections.emptyMap();

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public List<ConsumerConfig> getConsumers() {
            return consumers;
        }

        public void setConsumers(List<ConsumerConfig> consumers) {
            this.consumers = consumers;
        }

        public Map<String, EndpointConfig> getQueueWriters() { return queueWriters; }

        public EndpointConfig getQueueWriterEndpointConfig(String name) { return queueWriters.get(name); }

        public void setQueueWriters(Map<String, EndpointConfig> queueWriters) { this.queueWriters = queueWriters; }
    }

    public static class ConsumerConfig {
        private Class<? extends Consumer> consumer;
        private boolean enabled = true;
        private int concurrency = 1;
        private List<EndpointConfig> endpoints = Collections.emptyList();

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public Class<? extends Consumer> getConsumer() {
            if (consumer == null) {
                throw new IllegalArgumentException("A consumer implementation must be configured");
            }
            return consumer;
        }

        public void setConsumer(Class<? extends Consumer> consumer) {
            this.consumer = consumer;
        }

        public void setConsumer(String consumer) {
            try {
                this.consumer = Class.forName(consumer).asSubclass(Consumer.class);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        public List<EndpointConfig> getEndpoints() {
            return endpoints;
        }

        public void setEndpoints(List<EndpointConfig> endpoints) {
            this.endpoints = endpoints;
        }

        public int getConcurrency() {
            return concurrency;
        }

        public void setConcurrency(int concurrency) {
            this.concurrency = concurrency;
        }
    }

    public static class EndpointConfig {
        private String queueName;
        private String regionName;
        private String endpoint;

        public String getQueueName() {
            return queueName;
        }

        public void setQueueName(String queueName) {
            this.queueName = queueName;
        }

        public String getRegionName() {
            return regionName;
        }

        public void setRegionName(String regionName) {
            this.regionName = regionName;
        }

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public Optional<String> endpoint() {
            return Optional.ofNullable(endpoint);
        }
    }
}
