package com.quantis.risk_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quantis.risk_service.dto.OrderDto;
import com.quantis.risk_service.RiskEngine;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class OrderConsumer {

    private final ObjectMapper om = new ObjectMapper();
    private final RiskEngine riskEngine;

    public OrderConsumer(RiskEngine riskEngine) {
        this.riskEngine = riskEngine;
    }

    @KafkaListener(topics = "orders", containerFactory = "kafkaListenerContainerFactory")
    public void listen(String message) {
        try {
            OrderDto order = om.readValue(message, OrderDto.class);
            riskEngine.process(order);
        } catch (Exception e) {
            // log and optionally produce to a dead-letter topic
            e.printStackTrace();
        }
    }
}
