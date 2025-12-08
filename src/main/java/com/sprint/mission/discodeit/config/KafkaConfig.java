package com.sprint.mission.discodeit.config;

import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.PrioritizedParameterNameDiscoverer;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

@Configuration
@RequiredArgsConstructor
public class KafkaConfig {

    private final KafkaProperties kafkaProperties;

    /**
     * 브로드캐스트용 Kafka 리스너 컨테이너 팩토리 (채팅 메시지용) 각 인스턴스가 고유한 group-id를 갖도록 설정함
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> broadcastKafkaListenerContainerFactory(
        KafkaProperties kafkaProperties,
        @Value("${APP_NAME:discodeit}-${HOSTNAME:${random.uuid}}") String instanceId) {

        Map<String, Object> props = new HashMap<>(kafkaProperties.buildProducerProperties());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "chat-broadcast-" + instanceId);

        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        DefaultKafkaConsumerFactory<String, String> consumerFactory = new DefaultKafkaConsumerFactory<>(
            props);
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setMissingTopicsFatal(false);
        return factory;
    }


}
