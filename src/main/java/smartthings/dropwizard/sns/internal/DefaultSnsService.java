package smartthings.dropwizard.sns.internal;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.AmazonWebServiceRequest;
import com.amazonaws.ResponseMetadata;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.*;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.dropwizard.lifecycle.Managed;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.event.CircuitBreakerOnStateTransitionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import smartthings.dropwizard.sns.AmazonSNSProvider;
import smartthings.dropwizard.sns.SnsModule;
import smartthings.dropwizard.sns.SnsService;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static io.github.resilience4j.circuitbreaker.CircuitBreaker.decorateSupplier;

@Singleton
public class DefaultSnsService implements SnsService, Managed {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultSnsService.class);

    private final SnsModule.Config config;
    private final List<AmazonSNS> clients;
    private final AtomicReference<AmazonSNS> activeClient = new AtomicReference<>();
    private final CircuitBreaker breaker;
    private final LongAdder pos = new LongAdder();

    @Inject
    public DefaultSnsService(SnsModule.Config config, AmazonSNSProvider provider) {
        this.clients = config.isEnabled() ?
            config.getEndpoints()
                .stream()
                .map(provider::get)
                .collect(Collectors.toList()) : Collections.emptyList();
        this.config = config;
        this.breaker = buildCircuitBreaker();
        if (config.isEnabled()) {
            if (this.clients.isEmpty()) {
                throw new IllegalArgumentException("SNS must have at least 1 endpoint configured when enabled.");
            }
            this.activeClient.set(this.clients.get(0));
        }
    }

    @Override
    public void start() {
        LOG.info("Starting up SnsService...");
    }

    @Override
    public void stop() {
        LOG.info("Shutting down SnsService...");
        this.shutdown();
    }

    @Override
    public CreateTopicResult createTopic(CreateTopicRequest request) {
        LOG.trace("creating sns topic request={}", request);
        return decorateAndGet(() -> sns().createTopic(request));
    }

    @Override
    public SubscribeResult subscribe(SubscribeRequest request) {
        LOG.trace("subscribing to sns topic request={}", request);
        return decorateAndGet(() -> sns().subscribe(request));
    }

    @Override
    public PublishResult publish(PublishRequest request) {
        LOG.trace("publishing to sns topic request={}", request);
        return decorateAndGet(() -> sns().publish(request));
    }

    @Override
    public DeleteTopicResult deleteTopic(DeleteTopicRequest request) {
        LOG.debug("deleting sns topic request={}", request);
        return decorateAndGet(() -> sns().deleteTopic(request));
    }

    @Override
    public AddPermissionResult addPermission(AddPermissionRequest request) {
        return decorateAndGet(() -> sns().addPermission(request));
    }

    @Override
    public AddPermissionResult addPermission(
        String topicArn,
        String label,
        List<String> aWSAccountIds,
        List<String> actionNames
    ) {
        return decorateAndGet(() -> sns().addPermission(topicArn, label, actionNames, actionNames));
    }

    @Override
    public CheckIfPhoneNumberIsOptedOutResult checkIfPhoneNumberIsOptedOut(
        CheckIfPhoneNumberIsOptedOutRequest request
    ) {
        return decorateAndGet(() -> sns().checkIfPhoneNumberIsOptedOut(request));
    }

    @Override
    public ConfirmSubscriptionResult confirmSubscription(ConfirmSubscriptionRequest request) {
        return decorateAndGet(() -> sns().confirmSubscription(request));
    }

    @Override
    public ConfirmSubscriptionResult confirmSubscription(
        String topicArn,
        String token,
        String authenticateOnUnsubscribe
    ) {
        return decorateAndGet(() -> sns().confirmSubscription(topicArn, token, authenticateOnUnsubscribe));
    }

    @Override
    public ConfirmSubscriptionResult confirmSubscription(String topicArn, String token) {
        return decorateAndGet(() -> sns().confirmSubscription(topicArn, token));
    }

    @Override
    public CreatePlatformApplicationResult createPlatformApplication(
        CreatePlatformApplicationRequest request
    ) {
        return decorateAndGet(() -> sns().createPlatformApplication(request));
    }

    @Override
    public CreatePlatformEndpointResult createPlatformEndpoint(CreatePlatformEndpointRequest request) {
        return decorateAndGet(() -> sns().createPlatformEndpoint(request));
    }

    @Override
    public CreateTopicResult createTopic(String name) {
        return decorateAndGet(() -> sns().createTopic(name));
    }

    @Override
    public DeleteEndpointResult deleteEndpoint(DeleteEndpointRequest request) {
        return decorateAndGet(() -> sns().deleteEndpoint(request));
    }

    @Override
    public DeletePlatformApplicationResult deletePlatformApplication(
        DeletePlatformApplicationRequest request
    ) {
        return decorateAndGet(() -> sns().deletePlatformApplication(request));
    }

    @Override
    public DeleteTopicResult deleteTopic(String topicArn) {
        return decorateAndGet(() -> sns().deleteTopic(topicArn));
    }

    @Override
    public GetEndpointAttributesResult getEndpointAttributes(GetEndpointAttributesRequest request) {
        return decorateAndGet(() -> sns().getEndpointAttributes(request));
    }

    @Override
    public GetPlatformApplicationAttributesResult getPlatformApplicationAttributes(
        GetPlatformApplicationAttributesRequest request
    ) {
        return decorateAndGet(() -> sns().getPlatformApplicationAttributes(request));
    }

    @Override
    public GetSMSAttributesResult getSMSAttributes(GetSMSAttributesRequest request) {
        return decorateAndGet(() -> sns().getSMSAttributes(request));
    }

    @Override
    public GetSubscriptionAttributesResult getSubscriptionAttributes(
        GetSubscriptionAttributesRequest request
    ) {
        return decorateAndGet(() -> sns().getSubscriptionAttributes(request));
    }

    @Override
    public GetSubscriptionAttributesResult getSubscriptionAttributes(String subscriptionArn) {
        return decorateAndGet(() -> sns().getSubscriptionAttributes(subscriptionArn));
    }

    @Override
    public GetTopicAttributesResult getTopicAttributes(GetTopicAttributesRequest request) {
        return decorateAndGet(() -> sns().getTopicAttributes(request));
    }

    @Override
    public GetTopicAttributesResult getTopicAttributes(String topicArn) {
        return decorateAndGet(() -> sns().getTopicAttributes(topicArn));
    }

    @Override
    public ListEndpointsByPlatformApplicationResult listEndpointsByPlatformApplication(
        ListEndpointsByPlatformApplicationRequest request
    ) {
        return decorateAndGet(() -> sns().listEndpointsByPlatformApplication(request));
    }

    @Override
    public ListPhoneNumbersOptedOutResult listPhoneNumbersOptedOut(ListPhoneNumbersOptedOutRequest request) {
        return decorateAndGet(() -> sns().listPhoneNumbersOptedOut(request));
    }

    @Override
    public ListPlatformApplicationsResult listPlatformApplications(ListPlatformApplicationsRequest request) {
        return decorateAndGet(() -> sns().listPlatformApplications(request));
    }

    @Override
    public ListPlatformApplicationsResult listPlatformApplications() {
        return decorateAndGet(() -> sns().listPlatformApplications());
    }

    @Override
    public ListSubscriptionsResult listSubscriptions(ListSubscriptionsRequest request) {
        return decorateAndGet(() -> sns().listSubscriptions(request));
    }

    @Override
    public ListSubscriptionsResult listSubscriptions() {
        return decorateAndGet(() -> sns().listSubscriptions());
    }

    @Override
    public ListSubscriptionsResult listSubscriptions(String nextToken) {
        return decorateAndGet(() -> sns().listSubscriptions(nextToken));
    }

    @Override
    public ListSubscriptionsByTopicResult listSubscriptionsByTopic(ListSubscriptionsByTopicRequest request) {
        return decorateAndGet(() -> sns().listSubscriptionsByTopic(request));
    }

    @Override
    public ListSubscriptionsByTopicResult listSubscriptionsByTopic(String topicArn) {
        return decorateAndGet(() -> sns().listSubscriptionsByTopic(topicArn));
    }

    @Override
    public ListSubscriptionsByTopicResult listSubscriptionsByTopic(String topicArn, String nextToken) {
        return decorateAndGet(() -> sns().listSubscriptionsByTopic(topicArn, nextToken));
    }

    @Override
    public ListTopicsResult listTopics(ListTopicsRequest request) {
        return decorateAndGet(() -> sns().listTopics(request));
    }

    @Override
    public ListTopicsResult listTopics() {
        return decorateAndGet(() -> sns().listTopics());
    }

    @Override
    public ListTopicsResult listTopics(String nextToken) {
        return decorateAndGet(() -> sns().listTopics(nextToken));
    }

    @Override
    public OptInPhoneNumberResult optInPhoneNumber(OptInPhoneNumberRequest request) {
        return decorateAndGet(() -> sns().optInPhoneNumber(request));
    }

    @Override
    public PublishResult publish(String topicArn, String message) {
        return decorateAndGet(() -> sns().publish(topicArn, message));
    }

    @Override
    public PublishResult publish(String topicArn, String message, String subject) {
        return decorateAndGet(() -> sns().publish(topicArn, message, subject));
    }

    @Override
    public RemovePermissionResult removePermission(RemovePermissionRequest request) {
        return decorateAndGet(() -> sns().removePermission(request));
    }

    @Override
    public RemovePermissionResult removePermission(String topicArn, String label) {
        return decorateAndGet(() -> sns().removePermission(topicArn, label));
    }

    @Override
    public SetEndpointAttributesResult setEndpointAttributes(SetEndpointAttributesRequest request) {
        return decorateAndGet(() -> sns().setEndpointAttributes(request));
    }

    @Override
    public SetPlatformApplicationAttributesResult setPlatformApplicationAttributes(
        SetPlatformApplicationAttributesRequest request
    ) {
        return decorateAndGet(() -> sns().setPlatformApplicationAttributes(request));
    }

    @Override
    public SetSMSAttributesResult setSMSAttributes(SetSMSAttributesRequest request) {
        return decorateAndGet(() -> sns().setSMSAttributes(request));
    }

    @Override
    public SetSubscriptionAttributesResult setSubscriptionAttributes(
        SetSubscriptionAttributesRequest request
    ) {
        return decorateAndGet(() -> sns().setSubscriptionAttributes(request));
    }

    @Override
    public SetSubscriptionAttributesResult setSubscriptionAttributes(
        String subscriptionArn,
        String attributeName,
        String attributeValue
    ) {
        return decorateAndGet(() -> sns().setSubscriptionAttributes(subscriptionArn, attributeName, attributeValue));
    }

    @Override
    public SetTopicAttributesResult setTopicAttributes(SetTopicAttributesRequest request) {
        return decorateAndGet(() -> sns().setTopicAttributes(request));
    }

    @Override
    public SetTopicAttributesResult setTopicAttributes(
        String topicArn,
        String attributeName,
        String attributeValue
    ) {
        return decorateAndGet(() -> sns().setTopicAttributes(topicArn, attributeName, attributeValue));
    }

    @Override
    public SubscribeResult subscribe(String topicArn, String protocol, String endpoint) {
        return decorateAndGet(() -> sns().subscribe(topicArn, protocol, endpoint));
    }

    @Override
    public UnsubscribeResult unsubscribe(UnsubscribeRequest request) {
        return decorateAndGet(() -> sns().unsubscribe(request));
    }

    @Override
    public UnsubscribeResult unsubscribe(String subscriptionArn) {
        return decorateAndGet(() -> sns().unsubscribe(subscriptionArn));
    }

    @Override
    public ResponseMetadata getCachedResponseMetadata(AmazonWebServiceRequest request) {
        return decorateAndGet(() -> sns().getCachedResponseMetadata(request));
    }

    @Override
    public void triggerFailover() {
        int size = this.clients.size();

        if (size <= 1) {
            // No to additional clients to support failover.
            return;
        }

        pos.increment();
        int curr = this.pos.intValue();
        int next = curr >= size ? 0 : curr;

        if (next == 0) {
            pos.reset();
        }

        this.activeClient.set(this.clients.get(next));
    }

    @Override
    public void shutdown() {
        this.clients.forEach(AmazonSNS::shutdown);
    }

    private AmazonSNS sns() {
        if (!config.isEnabled()) {
            throw new IllegalStateException("Unable to execute SNS API when module is disabled.");
        }
        return activeClient.get();
    }

    private boolean isAwsServiceError(Throwable t) {
        if (t instanceof AmazonServiceException) {
            int status = ((AmazonServiceException) t).getStatusCode();
            return status >= 500 && status <= 599;
        }
        return false;
    }

    private void onStateChange(CircuitBreakerOnStateTransitionEvent event) {
        if (event.getStateTransition() == CircuitBreaker.StateTransition.CLOSED_TO_OPEN) {
            triggerFailover();
        }
    }

    private CircuitBreaker buildCircuitBreaker() {
        CircuitBreaker breaker = CircuitBreaker.of("sns", () ->
            CircuitBreakerConfig.custom()
                .recordFailure(this::isAwsServiceError)
                .build()
        );
        breaker.getEventPublisher().onStateTransition(this::onStateChange);
        return breaker;
    }

    private <T> T decorateAndGet(Supplier<T> supplier) {
        return decorateSupplier(breaker, supplier).get();
    }
}
