package com.cdc.service;

import com.cdc.config.KafkaConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Future;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.cdc.util.ObjectMapperUtil;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.mockito.*;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.BufferedReader;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@Configuration
@Import(KafkaConfig.class)
@EnableAutoConfiguration
@TestPropertySource(locations="classpath:application-test.properties")
public class KafkaCDCPublisherTest {
	@Mock
	private KafkaConfig kafkaConfig;

	@Mock
	private Properties kafkaProducerProperties;

	@Mock
	private KafkaProducer<String, String> producer;

	@InjectMocks
	private KafkaCDCPublisher kafkaCDCPublisher;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		kafkaConfig.setTopic("test");
	}

	@Test
	void testKafkaCDCIngestorInitialization() {
		assertNotNull(kafkaCDCPublisher);
	}

	@Test
	void testKafkaCDCIngestorWithValidConsumer() {
		KafkaCDCPublisher ingestor = new KafkaCDCPublisher(kafkaConfig, kafkaProducerProperties);
		assertNotNull(ingestor);
	}

	@Test
	void testKafkaCDCIngestorWithCustomConfig() {
		Map<String, Object> configs = new HashMap<>();
		configs.put("bootstrap.servers", "localhost:9092");
		configs.put("group.id", "test-group");

		KafkaCDCPublisher ingestor = new KafkaCDCPublisher(kafkaConfig, kafkaProducerProperties);
		assertNotNull(ingestor);
	}

	@Test
	void testKafkaCDCIngestor() {
		Map<String, Object> configs = new HashMap<>();
		configs.put("bootstrap.servers", "localhost:9092");
		configs.put("group.id", "test-group");

		KafkaCDCPublisher ingestor = new KafkaCDCPublisher(kafkaConfig, kafkaProducerProperties);
		assertNotNull(ingestor);
	}

	@Test
	void testProcessContents_withValidData() throws IOException {
		// Arrange
		String testContent = "{\"after\": {\"key\": \"123\", \"value\": \"test\"}}";
		JsonNode jsonNode = ObjectMapperUtil.INSTANCE.getObjectMapper().readTree(testContent);

		when(kafkaConfig.getJsonlFilePath()).thenReturn("test.jsonl");
		when(kafkaProducerProperties.getProperty(any())).thenReturn("some-value");
		when(kafkaConfig.getTopic()).thenReturn("test-topic");

		ClassPathResource resource = mock(ClassPathResource.class);
		BufferedReader reader = mock(BufferedReader.class);
		when(resource.getInputStream()).thenReturn(getClass().getResourceAsStream("/test.jsonl"));
		when(reader.readLine()).thenReturn(testContent, null); // Simulate reading one line
        when(kafkaConfig.getTopic()).thenReturn("test-topic");

		// Mocking the producer's send method
		doNothing().when(producer).send(any(ProducerRecord.class), any());

		kafkaCDCPublisher.init();

		// Act & Assert
		verify(producer, times(1)).send(any(ProducerRecord.class), any());
	}

	@Test
	void testPublishMessage_success() {
		String key = "123";
		String message = "{\"after\": {\"key\": \"123\", \"value\": \"test\"}}";

        Future<RecordMetadata> future = mock(Future.class);
        when(producer.send(any(ProducerRecord.class), any())).thenReturn(future);

        kafkaCDCPublisher.publishMessage(key, message);

        // Assert
        // Verify that the getTopic() method was called
        verify(kafkaConfig, times(1)).getTopic();

        // Verify that send was called on the producer with a non-null topic
        verify(producer, times(1)).send(any(ProducerRecord.class), any());
		// Act
		kafkaCDCPublisher.publishMessage(key, message);
		verify(producer, times(1)).send(any(ProducerRecord.class), any());
	}

	@Test
	void testPublishMessage_failure() {
		String key = "123";
		String message = "{\"after\": {\"key\": \"123\", \"value\": \"test\"}}";
        when(kafkaConfig.getTopic()).thenReturn("test-topic");

		doThrow(new RuntimeException("Kafka failure")).when(producer).send(any(ProducerRecord.class), any());

		// Act & Assert
		assertThrows(RuntimeException.class, () -> kafkaCDCPublisher.publishMessage(key, message));
	}

}
