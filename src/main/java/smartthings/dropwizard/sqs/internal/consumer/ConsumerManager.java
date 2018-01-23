package smartthings.dropwizard.sqs.internal.consumer;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import io.dropwizard.lifecycle.Managed;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import smartthings.dropwizard.sqs.Consumer;
import smartthings.dropwizard.sqs.SqsModule;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Service responsible for managing the lifecycle of all SQS Consumer implementations defined within Registry.
 */
@Singleton
public class ConsumerManager implements Managed {

    private static final Logger LOG = LoggerFactory.getLogger(ConsumerManager.class);

    private final Injector injector;
    private final SqsModule.Config config;
    private final SqsManager sqsManager;
    private List<ConsumerAction> actions = new ArrayList<>();
    private List<CircuitBreaker> breakers = new ArrayList<>();

    @Inject
    public ConsumerManager(Injector injector, SqsModule.Config config, SqsManager sqsManager) {
        this.injector = injector;
        this.config = config;
        this.sqsManager = sqsManager;
    }

    @Override
    public void start() {
        if (config.isEnabled()) {
            LOG.debug("Starting up SQS ConsumerManager...");
            init();
        } else {
            LOG.debug("Skipping start up of SQS ConsumerManager...");
        }
    }

    @Override
    public void stop() {
        LOG.debug("Shutting down SQS ConsumerManager...");
        actions.forEach(ConsumerAction::shutdown);
        actions.forEach(ConsumerAction::awaitShutdown);
    }

    public void pause() {
        this.breakers.forEach(CircuitBreaker::transitionToOpenState);
    }

    public void resume() {
        this.breakers.forEach(CircuitBreaker::transitionToClosedState);
    }

    private void init() {
        this.actions = config.getConsumers().stream()
            .flatMap(this::buildConsumerActions)
            .collect(Collectors.toList());

        // Kick off a new execution for each defined consumer.
        if (!this.actions.isEmpty()) {
            ExecutorService executor = Executors.newFixedThreadPool(actions.size());
            this.actions.forEach(executor::submit);
        }
    }

    private Stream<ConsumerAction> buildConsumerActions(SqsModule.ConsumerConfig config) {
        Consumer consumer = injector.getProvider(config.getConsumer()).get();
        return IntStream
            .rangeClosed(1, config.getConcurrency())
            .mapToObj(i -> config.getEndpoints())
            .flatMap(endpoints ->
                config.getEndpoints().stream()
                    .map(endpointConfig -> {
                        final String consumerKey = String.format("sqs-%s", endpointConfig.getQueueName());
                        CircuitBreaker breaker = CircuitBreaker.ofDefaults(consumerKey);
                        LOG.debug(
                            "Creating an SQS Consumer for class={}, queue={}",
                            config.getConsumer().getSimpleName(), consumerKey
                        );
                        breakers.add(breaker);
                        return new ConsumerAction(
                            sqsManager.get(endpointConfig),
                            consumer,
                            breaker,
                            endpointConfig
                        );
                    })
            );
    }
}
