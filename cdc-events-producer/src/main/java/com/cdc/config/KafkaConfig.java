package com.cdc.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import java.util.Properties;

@ConfigurationProperties(prefix = "cdc-config")
public class KafkaConfig {
	//default values
    private String topic;
    private String bootstrapServers;
    private String jsonlFilePath;


    public String getTopic() {
		return topic;
	}


	public void setTopic(String topic) {
		this.topic = topic;
	}


	public String getBootstrapServers() {
		return bootstrapServers;
	}


	public void setBootstrapServers(String bootstrapServers) {
		this.bootstrapServers = bootstrapServers;
	}


	public String getJsonlFilePath() {
		return jsonlFilePath;
	}


	public void setJsonlFilePath(String jsonlFilePath) {
		this.jsonlFilePath = jsonlFilePath;
	}


	@Bean
    public Properties kafkaProducerProperties() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        return props;
    }
}