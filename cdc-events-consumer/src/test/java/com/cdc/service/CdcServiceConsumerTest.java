package com.cdc.service;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.cdc.config.KafkaConfig;
import com.cdc.repository.CdcEventRepository;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

public class CdcServiceConsumerTest {

    @Mock
    private KafkaConfig kafkaConfig;

    @Mock
    private Properties kafkaProperties;

    @Mock
    private KafkaConsumer<String, String> consumer;

    @Mock
    private CdcEventRepository cdcEventRepository;

    @InjectMocks
    private CdcServiceConsumer cdcServiceConsumer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(kafkaConfig.getTopic()).thenReturn("test-topic");
        // Set the consumer to a mock instance
        cdcServiceConsumer.setTopicName("test-topic");
    }

    @Test
    void testConsumeMessages_success() {
        // Arrange
        String key = "123";
        String message = "{\"after\": {\"key\": \"123\", \"value\": \"test\"}}";
        ConsumerRecord<String, String> record = new ConsumerRecord<>(kafkaConfig.getTopic(), 0, 0, key, message);
        ConsumerRecords<String, String> consumerRecords = new ConsumerRecords<>(Collections.singletonMap(new org.apache.kafka.common.TopicPartition("test-topic", 0), Collections.singletonList(record)));

        // Mocking consumer behavior: when consumer.poll() is called, return the mocked consumer records
        when(consumer.poll(Duration.ofMillis(100))).thenReturn(consumerRecords);

        // Act
        // Call init to trigger consumeMessages (normally this would be called after application startup)
        cdcServiceConsumer.init();

        // Assert
        verify(cdcEventRepository, times(1)).persist(any(ConsumerRecord.class), eq("test-topic"));
    }

    @Test
    void testConsumeMessages_noRecords() {
        // Arrange
        ConsumerRecords<String, String> emptyRecords = new ConsumerRecords<>(Collections.emptyMap());

        // Mocking consumer behavior: when consumer.poll() is called, return empty consumer records
        when(consumer.poll(Duration.ofMillis(100))).thenReturn(emptyRecords);

        // Act
        // Call init to trigger consumeMessages (normally this would be called after application startup)
        cdcServiceConsumer.init();

        // Assert
        verify(cdcEventRepository, times(0)).persist(any(ConsumerRecord.class), eq("test-topic"));
    }

    @Test
    void testConsumeMessages_errorHandling() {
        // Arrange
        when(consumer.poll(Duration.ofMillis(100))).thenThrow(new RuntimeException("Kafka error"));

        // Act
        // Call init to trigger consumeMessages (normally this would be called after application startup)
        cdcServiceConsumer.init();

        // Assert: Check if the error log is called (we can mock the logger, or you can verify this manually)
        verify(consumer, times(1)).poll(Duration.ofMillis(100));
    }
}

