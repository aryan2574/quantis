package com.quantis.elasticsearch_writer_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot application class for the Elasticsearch Writer Service.
 * 
 * This service consumes trades from Kafka and writes searchable, indexed trade data to Elasticsearch.
 * It provides full-text search capabilities and complex analytics on trade data.
 */
@SpringBootApplication
public class ElasticsearchWriterServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ElasticsearchWriterServiceApplication.class, args);
    }
}
