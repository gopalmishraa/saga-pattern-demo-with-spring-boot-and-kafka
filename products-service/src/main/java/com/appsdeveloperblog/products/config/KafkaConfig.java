package com.appsdeveloperblog.products.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
public class KafkaConfig {

    @Value("${products.events.topic.name}")
    private String productsEventsTopicName;

    public static final Integer TOPIC_REPLICATION_FACTOR = 3;
    public static final Integer TOPIC_PARTITIONS = 3;

    private String ordersEventsTopicName;

    @Bean
    KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    NewTopic productsEventsTopic() {
        return TopicBuilder.name(productsEventsTopicName)
                .partitions(TOPIC_REPLICATION_FACTOR)
                .replicas(TOPIC_PARTITIONS)
                .build();
    }
}
