package com.cdc.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.cdc.config.KafkaConfig;
import com.cdc.util.Constants;
import com.cdc.util.ObjectMapperUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.annotations.VisibleForTesting;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@EnableConfigurationProperties(KafkaConfig.class)
public class KafkaCDCPublisher {
    private final KafkaConfig kafkaConfig;
    private final Properties kafkaProducerProperties;
    private KafkaProducer<String, String> producer;

    @Autowired
    public KafkaCDCPublisher(KafkaConfig kafkaConfig, Properties kafkaProducerProperties) {
        this.kafkaConfig = kafkaConfig;
        this.kafkaProducerProperties = kafkaProducerProperties;
    }

    @PostConstruct
    public void init() {
        // Initialize the Kafka Producer
        this.producer = new KafkaProducer<>(kafkaProducerProperties);
        processContents();
    }

    /**
     * Processes the content from the file and publishes each line as a message to Kafka.
     */
    private void processContents() {
        ClassPathResource resource = new ClassPathResource(kafkaConfig.getJsonlFilePath());

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
            String content;
            while ((content = reader.readLine()) != null) {
                if (content != null) {
                    JsonNode jsonNode = ObjectMapperUtil.INSTANCE.getObjectMapper().readTree(content);
                    String key = getKey(jsonNode);
                    if (key != null) {
                        publishMessage(key, content);
                    } else {
                        log.warn("No key found for message, skipping publish.");
                    }
                }
            }
        } catch (IOException e) {
            log.error("Failed to process content from file: {}", kafkaConfig.getJsonlFilePath(), e);
        } finally {
            // Ensure the producer is closed after processing
            closeProducer();
        }
    }

    /**
     * Extracts the key from the given JSON node.
     */
    private String getKey(JsonNode jsonNode) {
        JsonNode afterNode = jsonNode.path(Constants.TEXT_AFTER);
        if (afterNode != null && afterNode.has(Constants.TEXT_KEY)) {
            return afterNode.get(Constants.TEXT_KEY).asText();
        }
        return null;
    }

    /**
     * Publishes the message to Kafka with the given key.
     */
    @VisibleForTesting
    void publishMessage(String cdcEventMessageKey, String cdcEventMessage) {
        try {
            producer.send(new ProducerRecord<>(kafkaConfig.getTopic(), cdcEventMessageKey, cdcEventMessage), (metadata, exception) -> {
                if (exception != null) {
                    log.error("Failed to send message with key {} to Kafka topic {}: {}", cdcEventMessageKey, kafkaConfig.getTopic(), exception.getMessage());
                } else {
                    log.info("Message with key {} successfully sent to Kafka topic {}", cdcEventMessageKey, kafkaConfig.getTopic());
                }
            });
        } catch (Exception e) {
            log.error("Error occurred while sending message with key {} to Kafka", cdcEventMessageKey, e);
        }
    }

    /**
     * Ensures that the Kafka producer is closed gracefully.
     */
    private void closeProducer() {
        try {
            if (producer != null) {
                producer.close();
                log.info("Kafka producer closed successfully.");
            }
        } catch (Exception e) {
            log.error("Error closing Kafka producer: ", e);
        }
    }

    /**
     * Gracefully shuts down the Kafka producer if the application is stopped.
     */
    public void shutdown() {
        closeProducer();
    }
}
