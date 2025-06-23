package com.github.waitlight.quicktube.receiver;

import com.github.waitlight.quicktube.common.Msg;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

@ApplicationScoped
public class MessageProducerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageProducerService.class);
    private static final String TOPIC_NAME = "quicktube_messages";
    private static final String DEFAULT_BOOTSTRAP_SERVERS = "localhost:9092";

    private final KafkaProducer<String, Msg> producer;

    @Inject
    public MessageProducerService() {
        String bootstrapServers = System.getenv("KAFKA_BOOTSTRAP_SERVERS");
        if (bootstrapServers == null || bootstrapServers.isEmpty()) {
            bootstrapServers = DEFAULT_BOOTSTRAP_SERVERS;
        }

        LOGGER.info("Kafka连接地址: {}", bootstrapServers);

        Properties props = new Properties();
        props.put("bootstrap.servers", bootstrapServers);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "com.github.waitlight.quicktube.receiver.MsgSerializer");
        props.put("acks", "all");

        this.producer = new KafkaProducer<>(props);
    }

    public void sendMessage(Msg msg) {
        try {
            String key = msg.adr().getAdr();
            ProducerRecord<String, Msg> record = new ProducerRecord<>(TOPIC_NAME, key, msg);
            producer.send(record, (metadata, exception) -> {
                if (exception != null) {
                    LOGGER.error("消息发送失败", exception);
                } else {
                    LOGGER.info("消息发送成功 topic={}, partition={}, offset={}",
                            metadata.topic(), metadata.partition(), metadata.offset());
                }
            });
        } catch (Exception e) {
            LOGGER.error("发送消息到Kafka时发生错误", e);
            throw new RuntimeException("消息发送失败", e);
        }
    }
}