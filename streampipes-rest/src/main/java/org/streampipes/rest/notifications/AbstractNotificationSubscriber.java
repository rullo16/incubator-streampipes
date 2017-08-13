package org.streampipes.rest.notifications;

import org.streampipes.commons.config.Configuration;
import org.streampipes.messaging.EventConsumer;
import org.streampipes.messaging.kafka.SpKafkaConsumer;
import org.streampipes.model.client.messages.ProaSenseNotificationMessage;
import org.streampipes.storage.controller.StorageManager;
import org.apache.thrift.TDeserializer;
import org.apache.thrift.protocol.TBinaryProtocol;

import java.text.SimpleDateFormat;

/**
 * Created by riemer on 16.10.2016.
 */
public abstract class AbstractNotificationSubscriber implements EventConsumer<byte[]>, Runnable {

    protected String topic;
    protected TDeserializer deserializer;

    public AbstractNotificationSubscriber(String topic) {
        this.topic = topic;
        this.deserializer = new TDeserializer(new TBinaryProtocol.Factory());
    }

    public void subscribe() {
        SpKafkaConsumer kafkaConsumerGroup = new SpKafkaConsumer(Configuration.getInstance().getBrokerConfig().getKafkaUrl(), topic,
                this);
        Thread thread = new Thread(kafkaConsumerGroup);
        thread.start();
    }

    @Override
    public void run() {
        subscribe();
    }

    protected void storeNotification(ProaSenseNotificationMessage message) {
        StorageManager
                .INSTANCE
                .getNotificationStorageApi()
                .addNotification(message);
    }

    protected String parseDate(long timestamp) {
        return new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(timestamp);
    }

}
