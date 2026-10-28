package com.connectx.messaging.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaProducerConfig {

    public static final String CHAT_MESSAGES_TOPIC = "chat-messages";

    @Bean
    public NewTopic chatMessagesTopic() {
        return TopicBuilder.name(CHAT_MESSAGES_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
