package smartthings.dropwizard.sns;

import com.amazonaws.AmazonWebServiceRequest;
import com.amazonaws.ResponseMetadata;
import com.amazonaws.services.sns.model.*;

import java.util.List;

public interface SnsService {

    AddPermissionResult addPermission(
        AddPermissionRequest addPermissionRequest
    );

    AddPermissionResult addPermission(
        String topicArn,
        String label,
        List<String> aWSAccountIds,
        List<String> actionNames
    );

    CheckIfPhoneNumberIsOptedOutResult checkIfPhoneNumberIsOptedOut(
        CheckIfPhoneNumberIsOptedOutRequest checkIfPhoneNumberIsOptedOutRequest
    );

    ConfirmSubscriptionResult confirmSubscription(
        ConfirmSubscriptionRequest confirmSubscriptionRequest
    );

    ConfirmSubscriptionResult confirmSubscription(
        String topicArn,
        String token,
        String authenticateOnUnsubscribe
    );

    ConfirmSubscriptionResult confirmSubscription(
        String topicArn,
        String token
    );

    CreatePlatformApplicationResult createPlatformApplication(
        CreatePlatformApplicationRequest createPlatformApplicationRequest
    );

    CreatePlatformEndpointResult createPlatformEndpoint(
        CreatePlatformEndpointRequest createPlatformEndpointRequest
    );

    CreateTopicResult createTopic(
        CreateTopicRequest createTopicRequest
    );

    CreateTopicResult createTopic(
        String name
    );

    DeleteEndpointResult deleteEndpoint(DeleteEndpointRequest deleteEndpointRequest);

    DeletePlatformApplicationResult deletePlatformApplication(
        DeletePlatformApplicationRequest deletePlatformApplicationRequest
    );

    DeleteTopicResult deleteTopic(
        DeleteTopicRequest deleteTopicRequest
    );

    DeleteTopicResult deleteTopic(
        String topicArn
    );

    GetEndpointAttributesResult getEndpointAttributes(
        GetEndpointAttributesRequest getEndpointAttributesRequest
    );

    GetPlatformApplicationAttributesResult getPlatformApplicationAttributes(
        GetPlatformApplicationAttributesRequest getPlatformApplicationAttributesRequest
    );

    GetSMSAttributesResult getSMSAttributes(
        GetSMSAttributesRequest getSMSAttributesRequest
    );

    GetSubscriptionAttributesResult getSubscriptionAttributes(
        GetSubscriptionAttributesRequest getSubscriptionAttributesRequest
    );

    GetSubscriptionAttributesResult getSubscriptionAttributes(
        String subscriptionArn
    );

    GetTopicAttributesResult getTopicAttributes(
        GetTopicAttributesRequest getTopicAttributesRequest
    );

    GetTopicAttributesResult getTopicAttributes(
        String topicArn
    );

    ListEndpointsByPlatformApplicationResult listEndpointsByPlatformApplication(
        ListEndpointsByPlatformApplicationRequest listEndpointsByPlatformApplicationRequest
    );

    ListPhoneNumbersOptedOutResult listPhoneNumbersOptedOut(
        ListPhoneNumbersOptedOutRequest listPhoneNumbersOptedOutRequest
    );

    ListPlatformApplicationsResult listPlatformApplications(
        ListPlatformApplicationsRequest listPlatformApplicationsRequest
    );

    ListPlatformApplicationsResult listPlatformApplications();

    ListSubscriptionsResult listSubscriptions(
        ListSubscriptionsRequest listSubscriptionsRequest
    );

    ListSubscriptionsResult listSubscriptions();

    ListSubscriptionsResult listSubscriptions(
        String nextToken
    );

    ListSubscriptionsByTopicResult listSubscriptionsByTopic(
        ListSubscriptionsByTopicRequest listSubscriptionsByTopicRequest
    );

    ListSubscriptionsByTopicResult listSubscriptionsByTopic(
        String topicArn
    );

    ListSubscriptionsByTopicResult listSubscriptionsByTopic(
        String topicArn,
        String nextToken
    );

    ListTopicsResult listTopics(
        ListTopicsRequest listTopicsRequest
    );

    ListTopicsResult listTopics();

    ListTopicsResult listTopics(
        String nextToken
    );

    OptInPhoneNumberResult optInPhoneNumber(
        OptInPhoneNumberRequest optInPhoneNumberRequest
    );

    PublishResult publish(
        PublishRequest publishRequest
    );

    PublishResult publish(
        String topicArn,
        String message
    );

    PublishResult publish(
        String topicArn,
        String message,
        String subject
    );

    RemovePermissionResult removePermission(
        RemovePermissionRequest removePermissionRequest
    );

    RemovePermissionResult removePermission(
        String topicArn,
        String label
    );

    SetEndpointAttributesResult setEndpointAttributes(
        SetEndpointAttributesRequest setEndpointAttributesRequest
    );

    SetPlatformApplicationAttributesResult setPlatformApplicationAttributes(
        SetPlatformApplicationAttributesRequest setPlatformApplicationAttributesRequest
    );

    SetSMSAttributesResult setSMSAttributes(
        SetSMSAttributesRequest setSMSAttributesRequest
    );

    SetSubscriptionAttributesResult setSubscriptionAttributes(
        SetSubscriptionAttributesRequest setSubscriptionAttributesRequest
    );

    SetSubscriptionAttributesResult setSubscriptionAttributes(
        String subscriptionArn,
        String attributeName,
        String attributeValue
    );

    SetTopicAttributesResult setTopicAttributes(
        SetTopicAttributesRequest setTopicAttributesRequest
    );

    SetTopicAttributesResult setTopicAttributes(
        String topicArn,
        String attributeName,
        String attributeValue
    );

    SubscribeResult subscribe(
        SubscribeRequest subscribeRequest
    );

    SubscribeResult subscribe(
        String topicArn,
        String protocol,
        String endpoint
    );

    UnsubscribeResult unsubscribe(
        UnsubscribeRequest unsubscribeRequest
    );

    UnsubscribeResult unsubscribe(
        String subscriptionArn
    );

    ResponseMetadata getCachedResponseMetadata(
        AmazonWebServiceRequest request
    );

    void shutdown();

    void triggerFailover();
}
