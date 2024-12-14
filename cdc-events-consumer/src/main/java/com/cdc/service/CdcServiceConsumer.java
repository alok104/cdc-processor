package com.cdc.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import com.cdc.config.KafkaConfig;
import com.cdc.repository.CdcEventRepository;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

@Slf4j
@EnableConfigurationProperties(KafkaConfig.class)
@Component
public class CdcServiceConsumer {
	private final KafkaConfig kafkaConfig;
	private final Properties kafkaProperties;
	private KafkaConsumer<String, String> consumer;
	private CdcEventRepository cdcEventRepository;
	private String topicName;

	public void setTopicName(String topicName) {
		this.topicName = topicName;
	}

	@Autowired
	public CdcServiceConsumer(KafkaConfig kafkaConfig, Properties kafkaProperties, CdcEventRepository cdcEventRepository) {
		this.kafkaConfig = kafkaConfig;
		this.kafkaProperties = kafkaProperties;
		this.cdcEventRepository = cdcEventRepository;
	}

    @PostConstruct
    public void init() {
        // Configure Kafka Consumer
		consumer = new KafkaConsumer<>(kafkaProperties);
		consumeMessages();
    }
    
    /**
     * Start consuming messages from Kafka and persisting to OpenSearch.
     */
    private void consumeMessages() {
		try {
			consumer.subscribe(Collections.singletonList(kafkaConfig.getTopic()));
			while (true) {
				ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
				  // Check if no records received
                if (records.isEmpty()) {
                    log.debug("No records received in this poll.");
                    continue;
                }
				for (ConsumerRecord<String, String> record : records) {
					cdcEventRepository.persist(record, kafkaConfig.getTopic());
				}
			}
		} catch (Exception e) {
			log.error("Error processing data: ", e);
		} finally {
            consumer.close();
            log.info("Kafka Consumer closed");
        }

	}
    
}
