package smartthings.dropwizard.sqs;

import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.amazonaws.services.sqs.model.SendMessageResult;

import java.util.Map;

public interface QueueWriter {
    SendMessageResult sendMessage(String messageBody);

    SendMessageResult sendMessage(String messageBody, Integer delaySeconds);

    SendMessageResult sendMessage(String messageBody, Integer delaySeconds, Map<String, MessageAttributeValue> attributeValueMap);
}
