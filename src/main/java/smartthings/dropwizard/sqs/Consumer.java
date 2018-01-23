package smartthings.dropwizard.sqs;

import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;

/**
 * Interface for creating an SQS Consumer.  Implementers will have their consume method called as messages become
 * available.
 */
public interface Consumer {

    /**
     * Invoked upon message receipt from SQS.
     *
     * @param message the message being consumed
     * @throws Exception if an error occurs
     */
    void consume(Message message) throws Exception;

    /**
     * Override to provide defaults to the sqs message request.
     *
     * @return the <code>ReceiveMessageRequest</code>
     */
    default ReceiveMessageRequest getReceiveMessageRequest() {
        ReceiveMessageRequest request = new ReceiveMessageRequest();
        request.setWaitTimeSeconds(20);
        return request;
    }
}
