package smartthings.dropwizard.sqs;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.*;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.dropwizard.lifecycle.Managed;

/**
 * Default implementation for communicating with AWS SQS.
 */
@Singleton
public class DefaultSqsService implements SqsService, Managed {

    private final AmazonSQS sqs;

    @Inject
    public DefaultSqsService(AmazonSQS sqs) {
        this.sqs = sqs;
    }

    public void start() {
    }

    public void stop() {
        sqs.shutdown();
    }

    @Override
    public DeleteMessageResult deleteMessage(DeleteMessageRequest request) {
        return sqs.deleteMessage(request);
    }

    @Override
    public SendMessageResult sendMessage(SendMessageRequest request) {
        return sqs.sendMessage(request);
    }

    @Override
    public ReceiveMessageResult receiveMessage(ReceiveMessageRequest request) {
        return sqs.receiveMessage(request);
    }

    @Override
    public GetQueueUrlResult getQueueUrl(String queueName) {
        return sqs.getQueueUrl(queueName);
    }
}
