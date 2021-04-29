package smartthings.dropwizard.sqs

import com.amazonaws.services.sns.model.PublishRequest
import com.amazonaws.services.sns.model.PublishResult
import com.amazonaws.services.sqs.model.Message
import com.amazonaws.services.sqs.model.MessageAttributeValue
import com.amazonaws.services.sqs.model.SendMessageResult
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.inject.Inject
import com.google.inject.Stage
import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Environment
import io.dropwizard.testing.junit.DropwizardAppRule
import org.apache.commons.lang3.StringUtils
import org.asynchttpclient.AsyncHttpClient
import org.asynchttpclient.DefaultAsyncHttpClient
import org.asynchttpclient.Response
import org.junit.ClassRule
import smartthings.dropwizard.aws.AwsModule
import smartthings.dropwizard.sns.SnsModule
import smartthings.dropwizard.sns.SnsService
import smartthings.dropwizard.sqs.internal.consumer.ConsumerManager
import smartthings.dropwizard.sqs.internal.consumer.SqsManager
import smartthings.dw.guice.AbstractDwModule
import smartthings.dw.guice.DwGuice
import smartthings.dw.guice.WebResource
import spock.lang.Shared
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

import javax.ws.rs.POST
import javax.ws.rs.Path
import java.util.concurrent.atomic.AtomicInteger

class ConsumerFunctionalSpec extends Specification {

    @Shared
    String awsEndpointUrl = getSetting('awsEndpointUrl', "http://localhost:4100")

    @Shared
    ObjectMapper objectMapper = new ObjectMapper()

    @Shared
    AsyncHttpClient client = new DefaultAsyncHttpClient()

    @Shared
    TestConsumer consumer = new TestConsumer(objectMapper)

    @ClassRule
    @Shared
    DropwizardAppRule<TestConfiguration> app = new DropwizardAppRule<>(
        TestApplication,
        new TestConfiguration(
            consumer: consumer,
            aws: new AwsModule.Config(
                awsAccessKey: '<access-key>',
                awsSecretKey: '<secret-key>'
            ),
            sns: new SnsModule.Config(
                enabled: true,
                endpoints: [
                    new SnsModule.EndpointConfig(
                        regionName: 'us-east-1',
                        endpoint: awsEndpointUrl
                    )
                ]
            ),
            sqs: new SqsModule.Config(
                enabled: true,
                consumers: [
                    new SqsModule.ConsumerConfig(
                        consumer: TestConsumer,
                        endpoints: [
                            new SqsModule.EndpointConfig(
                                queueName: 'functional_test_queue',
                                regionName: 'us-east-1',
                                endpoint: awsEndpointUrl
                            )
                        ]
                    ),
                    new SqsModule.ConsumerConfig(
                        consumer: TestConsumer,
                        endpoints: [
                            new SqsModule.EndpointConfig(
                                queueName: 'simple_test_queue',
                                regionName: 'us-east-1',
                                endpoint: awsEndpointUrl
                            )
                        ]
                    )
                ],
                queueWriters: [
                    'queueWriter1': new SqsModule.EndpointConfig(
                        queueName: 'simple_test_queue',
                        regionName: 'us-east-1',
                        endpoint: awsEndpointUrl)
                ]
            )
        )
    )

    void 'it should publish and consume a message'() {
        given:
        PollingConditions conditions = new PollingConditions()
        TestMessage request = new TestMessage(message: UUID.randomUUID())

        when:
        Response response = client.preparePost("http://localhost:${app.getLocalPort()}/publish")
            .setHeader('Accept', 'application/json')
            .setHeader('Content-Type', 'application/json')
            .setBody(objectMapper.writeValueAsString(request))
            .execute()
            .get()

        then:
        response.statusCode == 200
        conditions.within(5) {
            consumer.callCount(request) == 1
        }
    }

    // this test will use a simple queue publish, not using SNS
    void 'it should publish and consume a message using QueueWriter'() {
        given:
        PollingConditions conditions = new PollingConditions()
        TestMessage request = new TestMessage(message: UUID.randomUUID())

        when:
        Response response = client.preparePost("http://localhost:${app.getLocalPort()}/queueWriter")
                .setHeader('Accept', 'application/json')
                .setHeader('Content-Type', 'application/json')
                .setBody(objectMapper.writeValueAsString(request))
                .execute()
                .get()

        then:
        response.statusCode == 200
        conditions.within(5) {
            consumer.callCount(request) == 1
        }
    }

    // this test will use a simple queue publish with delay, not using SNS
    // note that it does not appear that pafortin/goaws docker container implements the delay feature
    void 'it should publish and consume a message using QueueWriter with delay'() {
        given:
        PollingConditions conditions = new PollingConditions(timeout: 10)
        TestMessage request = new TestMessage(message: UUID.randomUUID())

        when:
        Response response = client.preparePost("http://localhost:${app.getLocalPort()}/queueWriterDelay")
                .setHeader('Accept', 'application/json')
                .setHeader('Content-Type', 'application/json')
                .setBody(objectMapper.writeValueAsString(request))
                .execute()
                .get()

        then:
        response.statusCode == 200
        conditions.within(8) {
            assert consumer.callCount(request) == 1
        }
    }

    // this test will use a simple queue publish with MessageAttributes, not using SNS
    void 'it should publish and consume a message using QueueWriter with attributes'() {
        given:
        PollingConditions conditions = new PollingConditions()
        TestMessage request = new TestMessage(message: UUID.randomUUID())

        when:
        Response response = client.preparePost("http://localhost:${app.getLocalPort()}/queueWriterAttributes")
                .setHeader('Accept', 'application/json')
                .setHeader('Content-Type', 'application/json')
                .setBody(objectMapper.writeValueAsString(request))
                .execute()
                .get()

        then:
        response.statusCode == 200
        conditions.within(5) {
            assert consumer.callCount(request) == 1
        }
    }

    void 'it should not delete message on a failure to consume'() {
        given:
        PollingConditions conditions = new PollingConditions()
        TestMessage request = new TestMessage(message: UUID.randomUUID())
        consumer.setConsumer {
            throw new UnsupportedOperationException('ohh no')
        }

        when:
        Response response = client.preparePost("http://localhost:${app.getLocalPort()}/publish")
            .setHeader('Accept', 'application/json')
            .setHeader('Content-Type', 'application/json')
            .setBody(objectMapper.writeValueAsString(request))
            .execute()
            .get()

        then:
        response.statusCode == 200
        conditions.within(45) {
            consumer.callCount(request) >= 2
        }
    }

    void 'it should be able to recover from circuit being opened'() {
        given:
        PollingConditions conditions = new PollingConditions()
        TestMessage request = new TestMessage(message: UUID.randomUUID())
        consumer.setConsumer { }

        when:
        Response response = client.preparePost("http://localhost:${app.getLocalPort()}/circuit/open")
            .execute()
            .get()

        then:
        response.statusCode == 204

        when:
        response = client.preparePost("http://localhost:${app.getLocalPort()}/publish")
            .setHeader('Accept', 'application/json')
            .setHeader('Content-Type', 'application/json')
            .setBody(objectMapper.writeValueAsString(request))
            .execute()
            .get()

        then:
        response.statusCode == 200

        when:
        response = client.preparePost("http://localhost:${app.getLocalPort()}/circuit/close")
            .execute()
            .get()

        then:
        response.statusCode == 204
        conditions.within(5) {
            consumer.callCount(request) == 1
        }
    }

    static String getSetting(String name, String defaultValue = null) {
        return System.getProperty(name) ?: System.getenv(toEnvFormat(name)) ?: defaultValue
    }

    static String toEnvFormat(String text) {
        text ==~ /^[A-Z_]+$/ ?
            text :
            text.replaceAll(/([A-Z])/, /_$1/).toUpperCase()
                .replaceAll(/^_/, '')
                .replaceAll(/\._?/, '__')
    }
}

class TestMessage {
    String message

    boolean equals(o) {
        if (this.is(o)) {
            return true
        }
        if (getClass() != o.class) {
            return false
        }

        TestMessage that = (TestMessage) o

        return message == that.message
    }

    int hashCode() {
        return (message != null ? message.hashCode() : 0)
    }
}

class TestConsumer implements Consumer {
    ObjectMapper mapper
    Map<TestMessage, AtomicInteger> messages = [:]
    java.util.function.Consumer<Message> consumer = { }

    TestConsumer(ObjectMapper mapper) {
        this.mapper = mapper
    }

    @Override
    void consume(Message message) throws Exception {
        // messages consumed from a simple queue publish do not contain the SNS attributes
        TestMessage testMessage
        if (StringUtils.contains(message.body, "\"TopicArn\"")) {
            Map body = mapper.readValue(message.body, new TypeReference<Map<String, Object>>() { })
            testMessage = mapper.readValue(body['Message'] as String, TestMessage)
        } else {
            testMessage = mapper.readValue(message.body as String, TestMessage)
        }
        if (messages.containsKey(testMessage)) {
            messages.get(testMessage).incrementAndGet()
        } else {
            messages.put(testMessage, new AtomicInteger(1))
        }
        consumer.accept(message)
    }

    int callCount(TestMessage message) {
        return messages.get(message)?.get() ?: 0
    }

    void setConsumer(java.util.function.Consumer<Message> consumer) {
        this.consumer = consumer
    }
}

class TestApplication extends Application<TestConfiguration> {
    @Override
    void run(TestConfiguration configuration, Environment environment) throws Exception {
        DwGuice.from(Stage.PRODUCTION,
            new TestModule(configuration),
        ).register(environment)
    }
}

class TestConfiguration extends Configuration {
    TestConsumer consumer

    AwsModule.Config aws
    SnsModule.Config sns
    SqsModule.Config sqs
}

class TestModule extends AbstractDwModule {

    TestConfiguration config

    TestModule(TestConfiguration config) {
        this.config = config
    }

    @Override
    protected void configure() {
        bind(AwsModule.Config).toInstance(config.aws)
        bind(SnsModule.Config).toInstance(config.sns)
        bind(SqsModule.Config).toInstance(config.sqs)
        bind(TestConsumer).toInstance(config.consumer)

        install(new AwsModule())
        install(new SnsModule())
        install(new SqsModule())
        registerResource(TestScopeResource)
    }
}

@Path("/")
class TestScopeResource implements WebResource {

    private final String topicArn = getSetting('topicArn', 'arn:aws:sns:local:000000000000:functional_test_queue')
    private final ObjectMapper objectMapper
    private final SnsService snsService
    private final ConsumerManager consumerManager
    private final SqsManager sqsManager

    @Inject
    TestScopeResource(
        SnsService snsService,
        ConsumerManager consumerManager,
        ObjectMapper objectMapper,
        SqsManager sqsManager
    ) {
        this.snsService = snsService
        this.consumerManager = consumerManager
        this.objectMapper = objectMapper
        this.sqsManager = sqsManager
    }

    @POST
    @Path('/queueWriter')
    SendMessageResult methodQueueWriter(TestMessage request) {
        QueueWriter queueWriter = sqsManager.getQueueWriter('queueWriter1')
        return queueWriter.sendMessage(objectMapper.writeValueAsString(request))
    }

    @POST
    @Path('/queueWriterDelay')
    SendMessageResult methodQueueWriterDelay(TestMessage request) {
        QueueWriter queueWriter = sqsManager.getQueueWriter('queueWriter1')
        return queueWriter.sendMessage(objectMapper.writeValueAsString(request), 5)
    }

    @POST
    @Path('/queueWriterAttributes')
    SendMessageResult methodQueueWriterAttributes(TestMessage request) {
        QueueWriter queueWriter = sqsManager.getQueueWriter('queueWriter1')
        Map<String, MessageAttributeValue> attributeValueMap = new HashMap<>();
        attributeValueMap.put("testAttr",
                new MessageAttributeValue().withDataType("String").withStringValue("test value"));
        return queueWriter.sendMessage(objectMapper.writeValueAsString(request), null, attributeValueMap)
    }

    @POST
    @Path('/publish')
    PublishResult methodPublish(TestMessage request) {
        def result = snsService.publish(
            new PublishRequest(
                topicArn,
                objectMapper.writeValueAsString(request)
            )
        )
        print result
        return result
    }

    @POST
    @Path('circuit/open')
    javax.ws.rs.core.Response methodCircuitOpen() {
        consumerManager.pause()
        return javax.ws.rs.core.Response.noContent().build()
    }

    @POST
    @Path('circuit/close')
    javax.ws.rs.core.Response methodCircuitClose() {
        consumerManager.resume()
        return javax.ws.rs.core.Response.noContent().build()
    }

    static String getSetting(String name, String defaultValue = null) {
        def val = System.getProperty(name) ?: System.getenv(toEnvFormat(name)) ?: defaultValue
        print val
        return val
    }

    static String toEnvFormat(String text) {
        text ==~ /^[A-Z_]+$/ ?
            text :
            text.replaceAll(/([A-Z])/, /_$1/).toUpperCase()
                .replaceAll(/^_/, '')
                .replaceAll(/\._?/, '__')
    }
}
