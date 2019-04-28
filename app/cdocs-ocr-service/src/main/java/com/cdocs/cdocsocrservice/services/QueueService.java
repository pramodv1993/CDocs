package com.cdocs.cdocsocrservice.services;

import com.cdocs.cdocsocrservice.CDocsProperties;
import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class QueueService {

    private static CDocsProperties cDocsProperties = new CDocsProperties();
    public static void main(String[] args) throws IOException, TimeoutException {
        String QUEUE_NAME = cDocsProperties.getProp("CONSUMING_QUEUE");
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            BasicProperties basicProperties = delivery.getProperties();
            String docName = String.valueOf(basicProperties.getHeaders().get("DOC_NAME"));
            TessaractService.recogniseAndPublish(delivery,docName);
            channel.basicAck(delivery.getEnvelope().getDeliveryTag(),false);
        };
        channel.basicConsume(QUEUE_NAME, false, deliverCallback ,consumerTag -> {});
    }

}
