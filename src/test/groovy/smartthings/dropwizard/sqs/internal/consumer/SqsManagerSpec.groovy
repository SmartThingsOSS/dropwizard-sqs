package smartthings.dropwizard.sqs.internal.consumer

import com.amazonaws.services.sqs.AmazonSQS
import com.amazonaws.services.sqs.model.GetQueueUrlResult
import com.amazonaws.services.sqs.model.Message
import smartthings.dropwizard.sqs.AmazonSQSProvider
import smartthings.dropwizard.sqs.Consumer
import smartthings.dropwizard.sqs.QueueWriter
import smartthings.dropwizard.sqs.SqsModule
import smartthings.dropwizard.sqs.SqsService
import spock.lang.Specification

class SqsManagerSpec extends Specification {

    AmazonSQS amazonSQS = Mock(AmazonSQS)

    AmazonSQSProvider sqsProvider = Mock(AmazonSQSProvider)

    SqsManager sqsManager

    void setup() {
        GetQueueUrlResult result = new GetQueueUrlResult().withQueueUrl("http://queue-url")
        amazonSQS.getQueueUrl(_) >> result
        sqsProvider.get(_) >> amazonSQS
    }

    void 'start method executes'() {
        given:
        sqsManager = new SqsManager(getConfig(true), sqsProvider)

        when:
        sqsManager.start()

        then:
        !sqsManager.sqsConsumerMap.isEmpty()
        !sqsManager.sqsQueueWriterMap.isEmpty()
    }

    void 'start method executes - not enabled'() {
        given:
        sqsManager = new SqsManager(getConfig(false), sqsProvider)

        when:
        sqsManager.start()

        then:
        sqsManager.sqsConsumerMap.isEmpty()
        sqsManager.sqsQueueWriterMap.isEmpty()
    }

    void 'get QueueWriter success'() {
        given:
        sqsManager = new SqsManager(getConfig(true), sqsProvider)

        when:
        sqsManager.start()
        QueueWriter queueWriter = sqsManager.getQueueWriter('writer1')

        then:
        queueWriter != null
    }

    void 'get QueueWriter fail'() {
        given:
        sqsManager = new SqsManager(getConfig(true), sqsProvider)

        when:
        sqsManager.start()
        QueueWriter queueWriter = sqsManager.getQueueWriter('writer-bad')

        then:
        thrown(IllegalStateException)
    }

    void 'get SqsService success'() {
        given:
        SqsModule.Config config = getConfig(true)
        sqsManager = new SqsManager(config, sqsProvider)

        when:
        sqsManager.start()
        SqsService sqsService = sqsManager.get(config.consumers[0].endpoints[0])

        then:
        sqsService != null
    }

    void 'get SqsService fail'() {
        given:
        sqsManager = new SqsManager(getConfig(true), sqsProvider)
        SqsModule.EndpointConfig dummyEndpointConfig = new SqsModule.EndpointConfig(
                queueName: 'queue-bad',
                endpoint: 'http://bad:4100/',
                regionName: 'bad'
        )

        when:
        sqsManager.start()
        SqsService sqsService = sqsManager.get(dummyEndpointConfig)

        then:
        thrown(IllegalStateException)
    }

    void 'missing region failure'() {
        given:
        SqsModule.Config config = getConfig(true)
        config.consumers[0].endpoints[0].regionName = null
        sqsManager = new SqsManager(config, sqsProvider)

        when:
        sqsManager.start()

        then:
        thrown(IllegalArgumentException)
    }

    void 'works with no consumers'() {
        given:
        SqsModule.Config config = getConfig(true)
        config.consumers = []
        sqsManager = new SqsManager(config, sqsProvider)

        when:
        sqsManager.start()
        QueueWriter queueWriter = sqsManager.getQueueWriter('writer1')

        then:
        queueWriter != null
    }

    void 'works with no queue writers'() {
        given:
        SqsModule.Config config = getConfig(true)
        config.queueWriters = [:]
        sqsManager = new SqsManager(config, sqsProvider)

        when:
        sqsManager.start()
        SqsService sqsService = sqsManager.get(config.consumers[0].endpoints[0])

        then:
        sqsService != null

        when:
        QueueWriter queueWriter = sqsManager.getQueueWriter('writer1')

        then:
        thrown(IllegalStateException)
    }

    void 'works with multiple queue writers'() {
        given:
        SqsModule.Config config = getConfig(true)
        config.consumers = []
        SqsModule.EndpointConfig writer2EndPt = new SqsModule.EndpointConfig(
                queueName: 'queue2',
                endpoint: 'http://localhost:4100/',
                regionName: 'us-northwest-32'
        )
        config.queueWriters.put('writer2', writer2EndPt)
        sqsManager = new SqsManager(config, sqsProvider)

        when:
        sqsManager.start()
        QueueWriter queueWriter = sqsManager.getQueueWriter('writer2')

        then:
        queueWriter != null
    }

    private SqsModule.Config getConfig(boolean enabled) {
        return new SqsModule.Config(
                enabled: enabled,
                consumers: [
                        new SqsModule.ConsumerConfig(
                                enabled: true,
                                consumer: TestConsumer,
                                endpoints: [
                                        new SqsModule.EndpointConfig(
                                                queueName: 'queue1',
                                                endpoint: 'http://localhost:4100/',
                                                regionName: 'us-east-1'
                                        )
                                ]
                        )
                ],
                queueWriters: [
                        'writer1': new SqsModule.EndpointConfig(
                                queueName: 'queue1',
                                endpoint: 'http://localhost:4100/',
                                regionName: 'us-east-1'
                        )
                ]
        )
    }
}

class TestConsumer implements Consumer {
    @Override
    void consume(Message message) throws Exception {

    }
}

