package smartthings.dropwizard.sqs.internal.services

import com.amazonaws.services.sqs.AmazonSQS
import com.amazonaws.services.sqs.model.*
import smartthings.dropwizard.sqs.DefaultSqsService
import spock.lang.Specification

class DefaultSqsServiceSpec extends Specification {

    AmazonSQS sqs = Mock(AmazonSQS)

    void setup() {
        0 * _
    }

    void 'it should delete a message'() {
        given:
        DefaultSqsService service = new DefaultSqsService(sqs)
        DeleteMessageRequest request = new DeleteMessageRequest()
        DeleteMessageResult response = new DeleteMessageResult()

        when:
        def result = service.deleteMessage(request)

        then:
        1 * sqs.deleteMessage(request) >> response

        and:
        assert result == response
    }

    void 'it should send a message'() {
        given:
        DefaultSqsService service = new DefaultSqsService(sqs)
        SendMessageRequest request = new SendMessageRequest()
        SendMessageResult response = new SendMessageResult()

        when:
        def result = service.sendMessage(request)

        then:
        1 * sqs.sendMessage(request) >> response

        and:
        assert result == response
    }

    void 'it should receive a message'() {
        given:
        DefaultSqsService service = new DefaultSqsService(sqs)
        ReceiveMessageRequest request = new ReceiveMessageRequest()
        ReceiveMessageResult response = new ReceiveMessageResult()

        when:
        def result = service.receiveMessage(request)

        then:
        1 * sqs.receiveMessage(request) >> response

        and:
        assert result == response
    }

    void 'it should get a queue url'() {
        given:
        DefaultSqsService service = new DefaultSqsService(sqs)
        String queueName = 'mars-10'
        GetQueueUrlResult response = new GetQueueUrlResult()

        when:
        def result = service.getQueueUrl(queueName)

        then:
        1 * sqs.getQueueUrl(queueName) >> response

        and:
        assert result == response
    }
}
