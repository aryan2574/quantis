package com.quantis.cassandra_writer_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;

/**
 * Main Spring Boot application class for the Cassandra Writer Service.
 * 
 * This service consumes trades from Kafka and writes time-series raw events to Cassandra.
 * It's optimized for high-throughput, low-latency writes of trade execution data.
 */
@SpringBootApplication
@EnableCassandraRepositories(basePackages = "com.quantis.cassandra_writer_service.repository")
public class CassandraWriterServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CassandraWriterServiceApplication.class, args);
    }
}
