package com.quantis.postgres_writer_service.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka configuration for the Postgres Writer Service.
 * 
 * This service consumes trade executions and persists them to PostgreSQL
 * with ACID compliance and batch processing capabilities.
 */
@Configuration
@EnableKafka
public class KafkaConfig {
    
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;
    
    /**
     * Consumer factory for processing trade messages
     */
    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, "postgres-writer-service-group");
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        
        // Performance optimizations for database persistence
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        configProps.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, 5000); // 5 seconds for database consistency
        configProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 100); // Smaller batches for database writes
        configProps.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 600000); // 10 minutes max poll interval
        configProps.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 60000); // 1 minute session timeout
        configProps.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 20000); // 20 seconds heartbeat
        
        // Memory and performance settings
        configProps.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, 1024); // Minimum fetch size
        configProps.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 500); // Max wait time for fetch
        configProps.put(ConsumerConfig.RECEIVE_BUFFER_CONFIG, 65536); // 64KB receive buffer
        configProps.put(ConsumerConfig.SEND_BUFFER_CONFIG, 131072); // 128KB send buffer
        
        // Database-specific settings
        configProps.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed"); // Read committed for consistency
        
        return new DefaultKafkaConsumerFactory<>(configProps);
    }
    
    /**
     * Kafka listener container factory with optimized settings for database writes
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = 
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        
        // Configure concurrency for parallel processing
        factory.setConcurrency(2); // Fewer threads for database consistency
        
        // Acknowledge mode for reliability
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.BATCH);
        
        // Error handling with retry
        factory.setCommonErrorHandler(new org.springframework.kafka.listener.DefaultErrorHandler());
        
        return factory;
    }
}
