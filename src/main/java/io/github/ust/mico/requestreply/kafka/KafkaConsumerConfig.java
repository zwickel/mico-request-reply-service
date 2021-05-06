package io.github.ust.mico.requestreply.kafka;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.SeekToCurrentErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer2;

import io.github.ust.mico.requestreply.MessageListener;
import lombok.extern.slf4j.Slf4j;

@EnableKafka
@Configuration
@Slf4j
public class KafkaConsumerConfig {

        @Value("${kafka.bootstrap-servers}")
        private String bootstrapServers;

        @Value("${kafka.group-id}")
        private String groupId;

        @Bean
        public Map<String, Object> consumerConfigs() {
                log.info("Using '{}' as bootstrap server", bootstrapServers);
                Map<String, Object> properties = new HashMap<>();
                properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
                properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer2.class);
                properties.put(ErrorHandlingDeserializer2.KEY_DESERIALIZER_CLASS, StringDeserializer.class);
                // https://docs.spring.io/spring-kafka/docs/2.2.0.RELEASE/reference/html/_reference.html#error-handling-deserializer
                properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer2.class);
                properties.put(ErrorHandlingDeserializer2.VALUE_DESERIALIZER_CLASS, StringDeserializer.class);
                properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
                properties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);

                return properties;
        }

        @Bean
        public ConsumerFactory<String, String> consumerFactory() {
                return new DefaultKafkaConsumerFactory<>(consumerConfigs());
        }

        @Bean
        public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, String>> kafkaListenerContainerFactory() {
                ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
                factory.setConsumerFactory(consumerFactory());
                factory.setErrorHandler(new SeekToCurrentErrorHandler(1));
                // TODO Add DeadLetterPublishingRecoverer later
                return factory;
        }

        @Bean
        public MessageListener receiver() {
                return new MessageListener();
        }
}
