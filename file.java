To cover the MessagePostProcessor line that sets a custom header, you can create a custom matcher or use an ArgumentCaptor to capture and verify the MessagePostProcessor argument when it’s used in the convertAndSend method. Here’s an updated version of the test case that includes verification for the MessagePostProcessor.

Updated Test Case

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.messaging.support.MessageBuilder;

class NotificationPublisherTest {

    @Mock
    private RabbitTemplate writeRabbitTemplateForScupMQ;

    @Mock
    private Logger logger;

    @InjectMocks
    private NotificationPublisher notificationPublisher;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        notificationPublisher.exchange = "test.exchange";
        notificationPublisher.routingKey = "test.routingKey";
    }

    @Test
    void testPublishEmail() {
        // Arrange
        EmailNotificationEvent event = new EmailNotificationEvent();
        Message<?> expectedMessage = MessageBuilder.withPayload(event)
            .setHeader(NotificationPublisher.HEADER_AIG_SOURCE_GEAR_ID, "catalog-6429")
            .build();

        // Mock the logger to verify debug message
        doNothing().when(logger).debug(anyString(), any());

        // Capture the MessagePostProcessor argument
        ArgumentCaptor<MessagePostProcessor> processorCaptor = ArgumentCaptor.forClass(MessagePostProcessor.class);

        // Act
        boolean result = notificationPublisher.publishEmail(event);

        // Assert
        assertTrue(result, "publishEmail should return true");

        // Verify that convertAndSend was called with correct arguments
        verify(writeRabbitTemplateForScupMQ, times(1)).convertAndSend(
            eq("test.exchange"),
            eq("test.routingKey"),
            eq(expectedMessage),
            processorCaptor.capture()
        );

        // Capture the MessagePostProcessor and apply it to a mock message to check the header
        Message mockMessage = mock(Message.class);
        MessageProperties mockMessageProperties = new MessageProperties();
        when(mockMessage.getMessageProperties()).thenReturn(mockMessageProperties);

        // Apply the captured MessagePostProcessor to the mock message
        processorCaptor.getValue().postProcessMessage(mockMessage);

        // Verify that the header is set correctly
        assertEquals("catalog-6429", mockMessageProperties.getHeaders().get(NotificationPublisher.HEADER_AIG_SOURCE_GEAR_ID));

        // Verify that the logger.debug was called
        verify(logger, times(1)).debug(contains("Email Notification to be sent"), eq(expectedMessage));
    }
}

Explanation

1. ArgumentCaptor: Captures the MessagePostProcessor used in convertAndSend.


2. Mock Message and Properties: Sets up a mock Message with MessageProperties so we can check if the header is set as expected.


3. Apply Captured MessagePostProcessor: Calls postProcessMessage on the mock message and verifies that the header HEADER_AIG_SOURCE_GEAR_ID is set to "catalog-6429".


4. Assertions: Checks that:

publishEmail returns true.

The convertAndSend method is called with the correct parameters.

The custom header is correctly set in the MessagePostProcessor.

The logger debug message is triggered.




This should provide full coverage for the MessagePostProcessor line in your code.

