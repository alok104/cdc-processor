package com.cdc.repository;

import java.io.IOException;

import org.apache.http.HttpHost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.action.index.IndexResponse;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestHighLevelClient;
import org.springframework.stereotype.Repository;

import com.cdc.config.OpenSearchConfig;
import com.cdc.util.ParqueFileConverter;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class CdcEventRepository {

    private static final String APPLICATION_OCTET_STREAM = "application/octet-stream";
    
    private RestHighLevelClient client;
    private final OpenSearchConfig openSearchConfig;
    public CdcEventRepository(OpenSearchConfig openSearchConfig) {
    	this.openSearchConfig = openSearchConfig;
    }
    
    @PostConstruct
    // Configure and create the OpenSearch client
    private void init() {
        client = new RestHighLevelClient(RestClient.builder(new HttpHost(openSearchConfig.getHost(), openSearchConfig.getPort(),
        		openSearchConfig.getScheme())));

	}

    /**
     * Persists a message to OpenSearch by converting it to Parquet format.
     * @param record the consumer record containing the message to be persisted
     * @param topic the topic name where the record should be indexed
     */
    public void persist(ConsumerRecord<String, String> record, String topic) {
        byte[] parquetBytes = convertToParquet(record.value());
        
        if (parquetBytes == null) {
            log.error("Failed to convert message to Parquet format for record: {}", record.key());
            return;
        }

        try {
            IndexResponse response = indexToOpenSearch(record.key(), topic, parquetBytes);
            log.debug("Successfully indexed record {} in OpenSearch: {}", record.key(), response.getResult());
        } catch (IOException e) {
            log.error("Error occurred while persisting record with key {} to OpenSearch", record.key(), e);
        }
    }

    /**
     * Converts the input string to Parquet byte array.
     * @param message the input message to be converted to Parquet format
     * @return byte[] representing the Parquet data
     */
    private byte[] convertToParquet(String message) {
        try {
            return ParqueFileConverter.convertStringToParquet(message);
        } catch (Exception e) {
            log.error("Error converting message to Parquet format", e);
            return null;
        }
    }

    /**
     * Creates an index request and sends it to OpenSearch.
     * @param key the record key to use as the document ID
     * @param topic the OpenSearch index (topic)
     * @param parquetBytes the parquet byte array to be indexed
     * @return IndexResponse the response from OpenSearch
     * @throws IOException if an error occurs while indexing
     */
    private IndexResponse indexToOpenSearch(String key, String topic, byte[] parquetBytes) throws IOException {
        IndexRequest request = new IndexRequest(topic)
                .id(key)
                .source(new ByteArrayEntity(parquetBytes), APPLICATION_OCTET_STREAM);

        return client.index(request, RequestOptions.DEFAULT);
    }

    /**
     * Closes the OpenSearch client when done.
     */
    public void close() throws IOException {
        client.close();
    }
}

