package com.payment.saga;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConfig {

//    @Bean
//    public NewTopic topic1() {
//        return TopicBuilder.name("order-created")
//                .partitions(3)
//                .replicas(1)
//                .build();
//    }
//
//    @Bean
//    public NewTopic topic2() {
//        return TopicBuilder.name("order-cancelled")
//                .partitions(5)
//                .replicas(2)
//                .build();
//    }

//    @Bean
//    public KafkaAdmin.NewTopics topics() {
//        return new KafkaAdmin.NewTopics(topic1(), topic2());
//    }

    @Bean
    public ProducerFactory<String, OrderCancelledEvent> producerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        // Producer configuration in Service A
        props.put(JsonSerializer.TYPE_MAPPINGS, "orderCancelled:com.payment.saga.OrderCancelledEvent");
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, OrderCancelledEvent> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
        //kafkaTemplate.setConsumerFactory(consumerFactory());
        //return kafkaTemplate;
    }

    @Bean
    public ConsumerFactory<String, OrderCreatedEvent> consumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*"); // for consumer specific
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, "payment-group");
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        // Consumer configuration in Service B
        configProps.put(JsonDeserializer.TYPE_MAPPINGS, "orderCreated:com.payment.saga.OrderCreatedEvent");

        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    @Bean
    public KafkaConsumer<String, OrderCreatedEvent> kafkaConsumer() {
        return (KafkaConsumer<String, OrderCreatedEvent>) consumerFactory().createConsumer();
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderCreatedEvent> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, OrderCreatedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }
}
