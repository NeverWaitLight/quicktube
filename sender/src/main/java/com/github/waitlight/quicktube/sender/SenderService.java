package com.github.waitlight.quicktube.sender;

import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.jboss.logging.Logger;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

@Startup
@ApplicationScoped
public class SenderService {

    private static final Logger LOG = Logger.getLogger(SenderService.class);

    @Inject
    MessageLogService messageLogService;

    private KafkaConsumer<String, String> kafkaConsumer;
    private ExecutorService executorService;
    private final AtomicBoolean running = new AtomicBoolean(false);

    @PostConstruct
    void init() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "quicktube-sender");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");

        kafkaConsumer = new KafkaConsumer<>(props);
        executorService = Executors.newVirtualThreadPerTaskExecutor();
        running.set(true);

        executorService.submit(() -> {
            kafkaConsumer.subscribe(Collections.singletonList("messages"));
            LOG.info("Subscribed to topic: messages");

            while (running.get()) {
                try {
                    kafkaConsumer.poll(Duration.ofSeconds(1))
                            .forEach(record -> {
                                executorService.submit(() -> {
                                    String message = record.value();
                                    String taskId = message.split(":")[0];
                                    String content = message.substring(taskId.length() + 1);
                                    LOG.infof("Received message: %s, task id: %s", content, taskId);

                                    // TODO: 消息组装、渠道选择和发送
                                    try {
                                        Thread.sleep(100); // 模拟消息发送
                                    } catch (InterruptedException e) {
                                         Thread.currentThread().interrupt();
                                     }

                                     // 写回发送结果
                                     LOG.info("Message sent successfully");
                                     messageLogService.logMessage(taskId, "SENT");
                                     kafkaConsumer.commitSync(); // 手动提交 offset
                                });
                            });
                } catch (Exception e) {
                    LOG.error("Error polling from Kafka", e);
                }
            }
        });
    }

    @PreDestroy
    void destroy() {
        running.set(false);
        if (kafkaConsumer != null) {
            kafkaConsumer.close();
        }
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
