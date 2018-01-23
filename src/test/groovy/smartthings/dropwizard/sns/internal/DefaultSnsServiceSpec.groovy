package smartthings.dropwizard.sns.internal

import com.amazonaws.AmazonServiceException
import com.amazonaws.AmazonWebServiceRequest
import com.amazonaws.ResponseMetadata
import com.amazonaws.services.sns.AmazonSNS
import com.amazonaws.services.sns.model.*
import smartthings.dropwizard.sns.AmazonSNSProvider
import smartthings.dropwizard.sns.SnsModule
import smartthings.dropwizard.sns.SnsService
import spock.lang.Specification
import spock.lang.Unroll

@SuppressWarnings(['MethodCount'])
class DefaultSnsServiceSpec extends Specification {

    SnsModule.Config config = new SnsModule.Config(
        enabled: true,
        endpoints: [
            new SnsModule.EndpointConfig(
                regionName: 'us-east-1',
                endpoint: 'http://localhost:4001'
            ),
            new SnsModule.EndpointConfig(
                regionName: 'us-east-2',
                endpoint: 'http://localhost:4002'
            ),
            new SnsModule.EndpointConfig(
                regionName: 'us-east-2',
                endpoint: 'http://localhost:4003'
            )
        ]
    )
    AmazonSNS client1 = Mock(AmazonSNS)
    AmazonSNS client2 = Mock(AmazonSNS)
    AmazonSNS client3 = Mock(AmazonSNS)
    AmazonSNSProvider provider = Mock(AmazonSNSProvider) {
        1 * get(config.endpoints.get(0)) >> client1
        1 * get(config.endpoints.get(1)) >> client2
        1 * get(config.endpoints.get(2)) >> client3
    }
    SnsService service

    void setup() {
        service = new DefaultSnsService(config, provider)
        0 * _
    }

    void 'it should create a topic'() {
        given:
        def request = new CreateTopicRequest()
        def result = new CreateTopicResult()

        when:
        def response = service.createTopic(request)

        then:
        1 * client1.createTopic(request) >> result

        assert response == result
    }

    void 'it should subscribe'() {
        given:
        def request = new SubscribeRequest()
        def result = new SubscribeResult()

        when:
        def response = service.subscribe(request)

        then:
        1 * client1.subscribe(request) >> result

        assert response == result
    }

    void 'it should publish'() {
        given:
        def request = new PublishRequest()
        def result = new PublishResult()

        when:
        def response = service.publish(request)

        then:
        1 * client1.publish(request) >> result

        assert response == result
    }

    void 'it should delete a topic'() {
        given:
        def request = new DeleteTopicRequest()
        def result = new DeleteTopicResult()

        when:
        def response = service.deleteTopic(request)

        then:
        1 * client1.deleteTopic(request) >> result

        assert response == result
    }

    void 'it should add permission'() {
        given:
        def request = new AddPermissionRequest()
        def result = new AddPermissionResult()

        when:
        def response = service.addPermission(request)

        then:
        1 * client1.addPermission(request) >> result

        assert response == result
    }

    void 'it should add permission simplified'() {
        given:
        String topicArn = 'arn'
        String label = 'label'
        List<String> accountIds = []
        List<String> actionNames = []
        def result = new AddPermissionResult()

        when:
        def response = service.addPermission(topicArn, label, accountIds, actionNames)

        then:
        1 * client1.addPermission(topicArn, label, accountIds, actionNames) >> result

        assert response == result
    }

    void 'it should check if phone number is opted out'() {
        given:
        def request = new CheckIfPhoneNumberIsOptedOutRequest()
        def result = new CheckIfPhoneNumberIsOptedOutResult()

        when:
        def response = service.checkIfPhoneNumberIsOptedOut(request)

        then:
        1 * client1.checkIfPhoneNumberIsOptedOut(request) >> result

        assert response == result
    }

    void 'it should confirm subscription'() {
        given:
        def request = new ConfirmSubscriptionRequest()
        def result = new ConfirmSubscriptionResult()

        when:
        def response = service.confirmSubscription(request)

        then:
        1 * client1.confirmSubscription(request) >> result

        assert response == result
    }

    void 'it should confirm subscription simplified #1'() {
        given:
        String topicArn = 'arn'
        String token = 'token'
        String authenticateOnUnsubscribe = 'auth'
        def result = new ConfirmSubscriptionResult()

        when:
        def response = service.confirmSubscription(topicArn, token, authenticateOnUnsubscribe)

        then:
        1 * client1.confirmSubscription(topicArn, token, authenticateOnUnsubscribe) >> result

        assert response == result
    }

    void 'it should confirm subscription simplified #2'() {
        given:
        String topicArn = 'arn'
        String token = 'token'
        def result = new ConfirmSubscriptionResult()

        when:
        def response = service.confirmSubscription(topicArn, token)

        then:
        1 * client1.confirmSubscription(topicArn, token) >> result

        assert response == result
    }

    void 'it should create platform application'() {
        given:
        def request = new CreatePlatformApplicationRequest()
        def result = new CreatePlatformApplicationResult()

        when:
        def response = service.createPlatformApplication(request)

        then:
        1 * client1.createPlatformApplication(request) >> result

        assert response == result
    }

    void 'it should create platform endpoint'() {
        given:
        def request = new CreatePlatformEndpointRequest()
        def result = new CreatePlatformEndpointResult()

        when:
        def response = service.createPlatformEndpoint(request)

        then:
        1 * client1.createPlatformEndpoint(request) >> result

        assert response == result
    }

    void 'it should create topic'() {
        given:
        String name = 'name'
        def result = new CreateTopicResult()

        when:
        def response = service.createTopic(name)

        then:
        1 * client1.createTopic(name) >> result

        assert response == result
    }

    void 'it should delete an endpoint'() {
        given:
        def request = new DeleteEndpointRequest()
        def result = new DeleteEndpointResult()

        when:
        def response = service.deleteEndpoint(request)

        then:
        1 * client1.deleteEndpoint(request) >> result

        assert response == result
    }

    void 'it should delete a platform application'() {
        given:
        def request = new DeletePlatformApplicationRequest()
        def result = new DeletePlatformApplicationResult()

        when:
        def response = service.deletePlatformApplication(request)

        then:
        1 * client1.deletePlatformApplication(request) >> result

        assert response == result
    }

    void 'it should delete a topic simplified'() {
        given:
        String topicArn = 'arn'
        def result = new DeleteTopicResult()

        when:
        def response = service.deleteTopic(topicArn)

        then:
        1 * client1.deleteTopic(topicArn) >> result

        assert response == result
    }

    void 'it should get endpoint attributes'() {
        given:
        def request = new GetEndpointAttributesRequest()
        def result = new GetEndpointAttributesResult()

        when:
        def response = service.getEndpointAttributes(request)

        then:
        1 * client1.getEndpointAttributes(request) >> result

        assert response == result
    }

    void 'it should get platform application attributes'() {
        given:
        def request = new GetPlatformApplicationAttributesRequest()
        def result = new GetPlatformApplicationAttributesResult()

        when:
        def response = service.getPlatformApplicationAttributes(request)

        then:
        1 * client1.getPlatformApplicationAttributes(request) >> result

        assert response == result
    }

    void 'it should get sms attributes'() {
        given:
        def request = new GetSMSAttributesRequest()
        def result = new GetSMSAttributesResult()

        when:
        def response = service.getSMSAttributes(request)

        then:
        1 * client1.getSMSAttributes(request) >> result

        assert response == result
    }

    void 'it should get subscription attributes'() {
        given:
        def request = new GetSubscriptionAttributesRequest()
        def result = new GetSubscriptionAttributesResult()

        when:
        def response = service.getSubscriptionAttributes(request)

        then:
        1 * client1.getSubscriptionAttributes(request) >> result

        assert response == result
    }

    void 'it should get subscription attributes simplified'() {
        given:
        String subscriptionArn = 'arn'
        def result = new GetSubscriptionAttributesResult()

        when:
        def response = service.getSubscriptionAttributes(subscriptionArn)

        then:
        1 * client1.getSubscriptionAttributes(subscriptionArn) >> result

        assert response == result
    }

    void 'it should get topic attributes'() {
        given:
        def request = new GetTopicAttributesRequest()
        def result = new GetTopicAttributesResult()

        when:
        def response = service.getTopicAttributes(request)

        then:
        1 * client1.getTopicAttributes(request) >> result

        assert response == result
    }

    void 'it should get topic attributes simplified'() {
        given:
        String topicArn = 'arn'
        def result = new GetTopicAttributesResult()

        when:
        def response = service.getTopicAttributes(topicArn)

        then:
        1 * client1.getTopicAttributes(topicArn) >> result

        assert response == result
    }

    void 'it should list endpoints by platform application'() {
        given:
        def request = new ListEndpointsByPlatformApplicationRequest()
        def result = new ListEndpointsByPlatformApplicationResult()

        when:
        def response = service.listEndpointsByPlatformApplication(request)

        then:
        1 * client1.listEndpointsByPlatformApplication(request) >> result

        assert response == result
    }

    void 'it should list phone number opted out'() {
        given:
        def request = new ListPhoneNumbersOptedOutRequest()
        def result = new ListPhoneNumbersOptedOutResult()

        when:
        def response = service.listPhoneNumbersOptedOut(request)

        then:
        1 * client1.listPhoneNumbersOptedOut(request) >> result

        assert response == result
    }

    void 'it should list platform applications'() {
        given:
        def request = new ListPlatformApplicationsRequest()
        def result = new ListPlatformApplicationsResult()

        when:
        def response = service.listPlatformApplications(request)

        then:
        1 * client1.listPlatformApplications(request) >> result

        assert response == result
    }

    void 'it should list platform applications simplified'() {
        given:
        def result = new ListPlatformApplicationsResult()

        when:
        def response = service.listPlatformApplications()

        then:
        1 * client1.listPlatformApplications() >> result

        assert response == result
    }

    void 'it should list subscriptions'() {
        given:
        def request = new ListSubscriptionsRequest()
        def result = new ListSubscriptionsResult()

        when:
        def response = service.listSubscriptions(request)

        then:
        1 * client1.listSubscriptions(request) >> result

        assert response == result
    }

    void 'it should list subscriptions simplified #1'() {
        given:
        def result = new ListSubscriptionsResult()

        when:
        def response = service.listSubscriptions()

        then:
        1 * client1.listSubscriptions() >> result

        assert response == result
    }

    void 'it should list subscriptions simplified #2'() {
        given:
        String nextToken = 'next'
        def result = new ListSubscriptionsResult()

        when:
        def response = service.listSubscriptions(nextToken)

        then:
        1 * client1.listSubscriptions(nextToken) >> result

        assert response == result
    }

    void 'it should list subscriptions by topic'() {
        given:
        def request = new ListSubscriptionsByTopicRequest()
        def result = new ListSubscriptionsByTopicResult()

        when:
        def response = service.listSubscriptionsByTopic(request)

        then:
        1 * client1.listSubscriptionsByTopic(request) >> result

        assert response == result
    }

    void 'it should list subscriptions by topic simplified #1'() {
        given:
        String topicArn = 'arn'
        def result = new ListSubscriptionsByTopicResult()

        when:
        def response = service.listSubscriptionsByTopic(topicArn)

        then:
        1 * client1.listSubscriptionsByTopic(topicArn) >> result

        assert response == result
    }

    void 'it should list subscriptions by topic simplified #2'() {
        given:
        String topicArn = 'arn'
        String nextToken = 'next'
        def result = new ListSubscriptionsByTopicResult()

        when:
        def response = service.listSubscriptionsByTopic(topicArn, nextToken)

        then:
        1 * client1.listSubscriptionsByTopic(topicArn, nextToken) >> result

        assert response == result
    }

    void 'it should list topics'() {
        given:
        def request = new ListTopicsRequest()
        def result = new ListTopicsResult()

        when:
        def response = service.listTopics(request)

        then:
        1 * client1.listTopics(request) >> result

        assert response == result
    }

    void 'it should list topics simplified #1'() {
        given:
        def result = new ListTopicsResult()

        when:
        def response = service.listTopics()

        then:
        1 * client1.listTopics() >> result

        assert response == result
    }

    void 'it should list topics simplified #2'() {
        given:
        String nextToken = 'next'
        def result = new ListTopicsResult()

        when:
        def response = service.listTopics(nextToken)

        then:
        1 * client1.listTopics(nextToken) >> result

        assert response == result
    }

    void 'it should set opt in phone number'() {
        given:
        def request = new OptInPhoneNumberRequest()
        def result = new OptInPhoneNumberResult()

        when:
        def response = service.optInPhoneNumber(request)

        then:
        1 * client1.optInPhoneNumber(request) >> result

        assert response == result
    }

    void 'it should publish simplified #1'() {
        given:
        String topicArn = 'arn'
        String message = 'message'
        def result = new PublishResult()

        when:
        def response = service.publish(topicArn, message)

        then:
        1 * client1.publish(topicArn, message) >> result

        assert response == result
    }

    void 'it should publish simplified #2'() {
        given:
        String topicArn = 'arn'
        String message = 'message'
        String subject = 'subject'
        def result = new PublishResult()

        when:
        def response = service.publish(topicArn, message, subject)

        then:
        1 * client1.publish(topicArn, message, subject) >> result

        assert response == result
    }

    void 'it should remove permission'() {
        given:
        def request = new RemovePermissionRequest()
        def result = new RemovePermissionResult()

        when:
        def response = service.removePermission(request)

        then:
        1 * client1.removePermission(request) >> result

        assert response == result
    }

    void 'it should remove permission simplified #1'() {
        given:
        String topicArn = 'arn'
        String label = 'label'
        def result = new RemovePermissionResult()

        when:
        def response = service.removePermission(topicArn, label)

        then:
        1 * client1.removePermission(topicArn, label) >> result

        assert response == result
    }

    void 'it should set endpoint attributes'() {
        given:
        def request = new SetEndpointAttributesRequest()
        def result = new SetEndpointAttributesResult()

        when:
        def response = service.setEndpointAttributes(request)

        then:
        1 * client1.setEndpointAttributes(request) >> result

        assert response == result
    }

    void 'it should set platform application attributes'() {
        given:
        def request = new SetPlatformApplicationAttributesRequest()
        def result = new SetPlatformApplicationAttributesResult()

        when:
        def response = service.setPlatformApplicationAttributes(request)

        then:
        1 * client1.setPlatformApplicationAttributes(request) >> result

        assert response == result
    }

    void 'it should set sms attributes'() {
        given:
        def request = new SetSMSAttributesRequest()
        def result = new SetSMSAttributesResult()

        when:
        def response = service.setSMSAttributes(request)

        then:
        1 * client1.setSMSAttributes(request) >> result

        assert response == result
    }

    void 'it should set subscription attributes'() {
        given:
        def request = new SetSubscriptionAttributesRequest()
        def result = new SetSubscriptionAttributesResult()

        when:
        def response = service.setSubscriptionAttributes(request)

        then:
        1 * client1.setSubscriptionAttributes(request) >> result

        assert response == result
    }

    void 'it should set subscription attributes simplified #1'() {
        given:
        String subscriptionArn = 'arn'
        String attributeName = 'attr'
        String attributeValue = 'value'
        def result = new SetSubscriptionAttributesResult()

        when:
        def response = service.setSubscriptionAttributes(subscriptionArn, attributeName, attributeValue)

        then:
        1 * client1.setSubscriptionAttributes(subscriptionArn, attributeName, attributeValue) >> result

        assert response == result
    }

    void 'it should set topic attributes'() {
        given:
        def request = new SetTopicAttributesRequest()
        def result = new SetTopicAttributesResult()

        when:
        def response = service.setTopicAttributes(request)

        then:
        1 * client1.setTopicAttributes(request) >> result

        assert response == result
    }

    void 'it should set topic attributes simplified #1'() {
        given:
        String topicArn = 'arn'
        String attributeName = 'attr'
        String attributeValue = 'value'
        def result = new SetTopicAttributesResult()

        when:
        def response = service.setTopicAttributes(topicArn, attributeName, attributeValue)

        then:
        1 * client1.setTopicAttributes(topicArn, attributeName, attributeValue) >> result

        assert response == result
    }

    void 'it should subscribe simplified #1'() {
        given:
        String topicArn = 'arn'
        String protocol = 'protocol'
        String endpoint = 'endpoint'
        def result = new SubscribeResult()

        when:
        def response = service.subscribe(topicArn, protocol, endpoint)

        then:
        1 * client1.subscribe(topicArn, protocol, endpoint) >> result

        assert response == result
    }

    void 'it should unsubscribe'() {
        given:
        def request = new UnsubscribeRequest()
        def result = new UnsubscribeResult()

        when:
        def response = service.unsubscribe(request)

        then:
        1 * client1.unsubscribe(request) >> result

        assert response == result
    }

    void 'it should unsubscribe simplified #1'() {
        given:
        String subscriptionArn = 'arn'
        def result = new UnsubscribeResult()

        when:
        def response = service.unsubscribe(subscriptionArn)

        then:
        1 * client1.unsubscribe(subscriptionArn) >> result

        assert response == result
    }

    void 'it should get cached response metadata'() {
        given:
        def request = AmazonWebServiceRequest.NOOP
        def result = new ResponseMetadata([:])

        when:
        def response = service.getCachedResponseMetadata(request)

        then:
        1 * client1.getCachedResponseMetadata(request) >> result

        assert response == result
    }

    void 'it should failover and publish'() {
        given:
        def request = new PublishRequest()
        def result = new PublishResult()

        when:
        def response = service.publish(request)

        then:
        1 * client1.publish(request) >> result

        assert response == result

        when:
        service.triggerFailover()

        response = service.publish(request)

        then:
        1 * client2.publish(request) >> result

        assert response == result
    }

    void 'it should support triggering a failover'() {
        expect:
        assert service.sns() == client1

        when:
        service.triggerFailover()

        then:
        assert service.sns() == client2

        when:
        service.triggerFailover()

        then:
        assert service.sns() == client3

        when:
        service.triggerFailover()

        then:
        assert service.sns() == client1
    }

    void 'it should skip failover when only 1 client'() {
        setup:
        AmazonSNS testClient = Mock(AmazonSNS)
        SnsModule.Config testConfig = new SnsModule.Config(
            enabled: true,
            endpoints: [
                new SnsModule.EndpointConfig(
                    regionName: 'us-east-1',
                    endpoint: 'http://localhost:5555'
                )
            ]
        )
        AmazonSNSProvider testProvider = Mock(AmazonSNSProvider) {
            1 * get(testConfig.endpoints.get(0)) >> testClient
        }
        SnsService snsService = new DefaultSnsService(testConfig, testProvider)

        expect:
        assert snsService.sns() == testClient

        when:
        snsService.triggerFailover()

        then:
        assert snsService.sns() == testClient
    }

    void 'it should throw an error if attempting to use SNS when disabled'() {
        given:
        service = new DefaultSnsService(new SnsModule.Config(enabled: false), provider)

        when:
        service.sns()

        then:
        thrown(IllegalStateException)
    }

    @Unroll
    void 'it should detect a #statusCode from AWS service'() {
        given:
        AmazonServiceException ase = new AmazonServiceException('oops')
        ase.statusCode = statusCode

        when:
        boolean result = service.isAwsServiceError(ase)

        then:
        assert result == isError

        where:
        statusCode | isError
        200        | false
        201        | false
        204        | false
        422        | false
        500        | true
        501        | true
        504        | true
        599        | true
        600        | false
    }

    void 'it should support shutdown of SNS clients'() {
        when:
        service.shutdown()

        then:
        1 * client1.shutdown()
        1 * client2.shutdown()
        1 * client3.shutdown()
    }
}
