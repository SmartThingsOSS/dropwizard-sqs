package smartthings.dropwizard.sqs;

import com.amazonaws.services.sqs.model.*;

/**
 * Supported AWS SQS operations.
 */
public interface SqsService {

    DeleteMessageResult deleteMessage(DeleteMessageRequest request);

    SendMessageResult sendMessage(SendMessageRequest request);

    ReceiveMessageResult receiveMessage(ReceiveMessageRequest request);

    GetQueueUrlResult getQueueUrl(String queueName);
}
