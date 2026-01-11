package com.orderly.order.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

import static com.orderly.common.constants.KafkaTopics.*;

/**
 * Kafka configuration.
 * Creates topics on startup if they don't exist.
 */
@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic orderPlacedTopic() {
        return TopicBuilder.name(ORDER_PLACED)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic orderConfirmedTopic() {
        return TopicBuilder.name(ORDER_CONFIRMED)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic orderFailedTopic() {
        return TopicBuilder.name(ORDER_FAILED)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
