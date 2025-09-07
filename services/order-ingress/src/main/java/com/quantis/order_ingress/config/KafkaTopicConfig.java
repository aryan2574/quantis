package com.quantis.order_ingress.config;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka Topic Configuration for Order Ingress Service.
 * This class automatically creates the required Kafka topics when the application starts.
 * 
 * In production, you might want to:
 * 1. Use external topic management (Kafka Manager, Confluent Control Center)
 * 2. Pre-create topics with specific configurations
 * 3. Use infrastructure-as-code (Terraform, Ansible)
 */
@Configuration
public class KafkaTopicConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    /**
     * KafkaAdmin bean that handles topic creation.
     * Spring Boot will automatically create topics defined as beans.
     */
    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new KafkaAdmin(configs);
    }

    /**
     * Creates the 'orders' topic for incoming orders.
     * 
     * Configuration:
     * - Partitions: 6 (for parallel processing)
     * - Replication Factor: 1 (single broker setup)
     * - Cleanup Policy: delete (remove old messages)
     */
    @Bean
    public NewTopic ordersTopic() {
        return TopicBuilder.name("orders")
                .partitions(6)
                .replicas(1)
                .config("cleanup.policy", "delete")
                .config("retention.ms", "604800000") // 7 days
                .build();
    }
}
