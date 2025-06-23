package com.github.waitlight.quicktube.receiver;

import com.github.waitlight.quicktube.common.Msg;
import org.apache.kafka.common.serialization.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Map;

public class MsgSerializer implements Serializer<Msg> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MsgSerializer.class);

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        // 无需配置
    }

    @Override
    public byte[] serialize(String topic, Msg data) {
        if (data == null) {
            return null;
        }

        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                ObjectOutputStream objectStream = new ObjectOutputStream(byteStream)) {

            objectStream.writeObject(data);
            return byteStream.toByteArray();

        } catch (IOException e) {
            LOGGER.error("序列化Msg对象失败", e);
            throw new RuntimeException("序列化失败", e);
        }
    }

    @Override
    public void close() {
        // 无需关闭资源
    }
}