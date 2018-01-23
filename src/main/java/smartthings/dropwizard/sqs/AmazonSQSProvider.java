package smartthings.dropwizard.sqs;

import com.amazonaws.services.sqs.AmazonSQS;

public interface AmazonSQSProvider {
    AmazonSQS get(SqsModule.EndpointConfig config);
}
