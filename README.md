# cdc-processor

## Overview
The **cdc-processor** project is designed to process Change Data Capture (CDC) events. It consists of two main components:

1. **cdc-events-producer**: This module ingests sample CDC events into a Kafka topic.
2. **cdc-events-consumer**: This module persists data from Kafka into OpenSearch.

## Get Started
Follow these steps to set up and run the project:

### 1. Start Dependencies
Run the following command to bring up Kafka, KafkaUI, and OpenSearch:
```bash
docker-compose up -d
```

### 2. Run the cdc-events-producer
This module ingests sample CDC events into a Kafka topic.

#### EntryPoint Class
Navigate to the following Java file and run it as a Java Application:

[**CdcEventProducerApplication.java**](https://github.com/alok104/cdc-processor/blob/main/cdc-events-producer/src/main/java/com/cdc/CdcEventProducerApplication.java)

### 3. Run the cdc-events-consumer
This module persists the data from Kafka into OpenSearch.

#### EntryPoint Class
Navigate to the following Java file and run it as a Java Application:

[**CdcEventsConsumerApplication.java**](https://github.com/alok104/cdc-processor/blob/main/cdc-events-consumer/src/main/java/com/cdc/CdcEventsConsumerApplication.java)

## Repository Structure
- **cdc-events-producer**: Contains the code for ingesting CDC events.
- **cdc-events-consumer**: Contains the code for persisting CDC data to OpenSearch.
- **docker-compose.yml**: Configuration file to bring up the required services (Kafka, KafkaUI, OpenSearch).

## Requirements
- Java 17 or higher
- Docker
- Docker Compose

## Notes
- Ensure Docker is installed and running on your system before executing the `docker-compose` command.
- Check the logs of each service using `docker logs` if you encounter any issues.
