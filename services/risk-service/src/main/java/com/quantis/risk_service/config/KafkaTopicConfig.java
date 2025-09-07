package com.quantis.risk_service.config;

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
 * Kafka Topic Configuration for Risk Service.
 * This class automatically creates the required Kafka topics when the application starts.
 */
@Configuration
public class KafkaTopicConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    /**
     * KafkaAdmin bean that handles topic creation.
     */
    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new KafkaAdmin(configs);
    }

    /**
     * Creates the 'orders.valid' topic for approved orders.
     * 
     * Configuration:
     * - Partitions: 6 (matches orders topic for parallel processing)
     * - Replication Factor: 1 (single broker setup)
     * - Cleanup Policy: delete (remove old messages)
     */
    @Bean
    public NewTopic ordersValidTopic() {
        return TopicBuilder.name("orders.valid")
                .partitions(6)
                .replicas(1)
                .config("cleanup.policy", "delete")
                .config("retention.ms", "604800000") // 7 days
                .build();
    }

    /**
     * Creates the 'orders.rejected' topic for rejected orders.
     * 
     * Configuration:
     * - Partitions: 3 (fewer partitions as rejections are typically less frequent)
     * - Replication Factor: 1 (single broker setup)
     * - Cleanup Policy: delete (remove old messages)
     */
    @Bean
    public NewTopic ordersRejectedTopic() {
        return TopicBuilder.name("orders.rejected")
                .partitions(3)
                .replicas(1)
                .config("cleanup.policy", "delete")
                .config("retention.ms", "2592000000") // 30 days (keep rejections longer for analysis)
                .build();
    }
}
