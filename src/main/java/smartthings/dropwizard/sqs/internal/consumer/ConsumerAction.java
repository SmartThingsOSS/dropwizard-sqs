package smartthings.dropwizard.sqs.internal.consumer;

import com.amazonaws.services.sqs.model.*;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.vavr.control.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import smartthings.dropwizard.aws.internal.backoff.ExponentialBackoff;
import smartthings.dropwizard.sqs.Consumer;
import smartthings.dropwizard.sqs.SqsModule;
import smartthings.dropwizard.sqs.SqsService;

import java.util.concurrent.atomic.AtomicBoolean;

import static io.github.resilience4j.circuitbreaker.CircuitBreaker.decorateCheckedSupplier;

/**
 * Action definition for continuous polling of SQS messages.
 */
public class ConsumerAction implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(ConsumerAction.class);

    private final SqsService sqs;
    private final Consumer consumer;
    private final SqsModule.EndpointConfig config;
    private String sqsQueueUrl;
    private AtomicBoolean shutdown = new AtomicBoolean(false);
    private AtomicBoolean shutdownComplete = new AtomicBoolean(false);
    private final CircuitBreaker breaker;
    private final ExponentialBackoff backoff = new ExponentialBackoff();
    private final Object mutex = new Object();

    public ConsumerAction(
        SqsService sqs,
        Consumer consumer,
        CircuitBreaker breaker,
        SqsModule.EndpointConfig config
    ) {
        this.sqs = sqs;
        this.consumer = consumer;
        this.config = config;
        this.breaker = breaker;
        this.breaker.getEventPublisher().onStateTransition(event -> {
            if (!isCircuitOpen()) {
                backoff.reset();
            }
        });
    }

    @Override
    public void run() {
        while (!shutdown.get()) {
            Try.run(this::poll)
                .onFailure(t -> {
                    log.error("Unexpected exception consumer={} terminated.", config.getQueueName(), t);
                    shutdown.set(true);
                });
        }
        notifyShutdown();
    }

    public void shutdown() {
        shutdown.set(true);
    }

    public void awaitShutdown() {
        synchronized (mutex) {
            while (!shutdownComplete.get()) {
                try {
                    mutex.wait();
                } catch (InterruptedException e) {
                    // Intentionally left blank
                }
            }
        }
        log.warn("SQS consumer={} shutdown complete.", config.getQueueName());
    }

    private void poll() {
        Try.run(() -> {
                maybeBackoff();
                consume(receiveMessage(getReceiveMessageRequest()));
            }
        ).onFailure(t -> log.error("Unexpected exception polling SQS", t));
    }

    private String getQueueUrl() {
        if (sqsQueueUrl != null) {
            return sqsQueueUrl;
        }

        String queueName = config.getQueueName();
        if (queueName == null || queueName.isEmpty()) {
            throw new IllegalArgumentException("An SQS Consumer must define a queue in which to poll.");
        }

        sqsQueueUrl = sqs.getQueueUrl(queueName).getQueueUrl();
        return sqsQueueUrl;
    }

    private void consume(ReceiveMessageResult result) {
        result.getMessages().forEach(this::consume);
    }

    private void consume(Message message) {
        Try.run(() -> consumer.consume(message))
            .andThen(() -> deleteMessage(message))
            .onFailure(t -> log.error("Failed to consume message.  message={}", message, t));
    }

    private ReceiveMessageResult receiveMessage(ReceiveMessageRequest request) {
        log.debug("Execute receiveMessage for SQS queue={}", config.getQueueName());
        return Try.of(decorateCheckedSupplier(breaker, () -> sqs.receiveMessage(request)))
            .recover(t -> new ReceiveMessageResult())
            .get();
    }

    private DeleteMessageResult deleteMessage(Message message) {
        DeleteMessageRequest request = new DeleteMessageRequest(getQueueUrl(), message.getReceiptHandle());
        return Try.of(decorateCheckedSupplier(breaker, () -> sqs.deleteMessage(request)))
            .recover(t -> new DeleteMessageResult())
            .get();
    }

    private ReceiveMessageRequest getReceiveMessageRequest() {
        ReceiveMessageRequest request = consumer.getReceiveMessageRequest();
        if (request.getQueueUrl() == null || request.getQueueUrl().isEmpty()) {
            request.withQueueUrl(getQueueUrl());
        }
        return request;
    }

    private void maybeBackoff() {
        if (isCircuitOpen()) {
            backoff.backoff();
        }
    }

    private boolean isCircuitOpen() {
        return CircuitBreaker.State.OPEN == breaker.getState();
    }

    private void notifyShutdown() {
        synchronized (mutex) {
            shutdownComplete.set(true);
            mutex.notifyAll();
        }
    }
}
