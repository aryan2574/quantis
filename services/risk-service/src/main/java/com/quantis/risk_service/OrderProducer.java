package com.quantis.risk_service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderProducer {
    private final KafkaTemplate<String, String> kafka;

    public OrderProducer(KafkaTemplate<String, String> kafka) {
        this.kafka = kafka;
    }

    public void publishValid(String key, String json) {
        kafka.send("orders.valid", key, json);
    }

    public void publishRejected(String key, String json) {
        kafka.send("orders.rejected", key, json);
    }
}
