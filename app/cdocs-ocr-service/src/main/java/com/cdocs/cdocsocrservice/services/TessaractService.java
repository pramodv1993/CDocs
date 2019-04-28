package com.cdocs.cdocsocrservice.services;

import com.cdocs.cdocsocrservice.CDocsProperties;
import com.rabbitmq.client.*;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

class TessaractService {
    private static CDocsProperties cDocsProperties = new CDocsProperties();
    private static String TMP_PATH = cDocsProperties.getProp("TMP_PATH");
    private static Tesseract tesseract = new Tesseract();
    static {
        tesseract.setDatapath(cDocsProperties.getProp("TSR_PATH"));
    }

    private static AMQP.BasicProperties constructQueueProps(BasicProperties initialProps){
        System.out.println("Publishing doc with id" + initialProps.getCorrelationId() + " to C_Docs_RecognisedData");
        return new AMQP.BasicProperties.Builder()
                .correlationId(initialProps.getCorrelationId())
                .build();
    }

    /**
     *
     * @param msg
     */
    private static void publishMessage(String msg, BasicProperties initialProps){
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        String QUEUE_NAME = cDocsProperties.getProp("PUBLISHING_QUEUE");
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
//            channel.addConfirmListener((ackTag, multiple)->{
//                System.out.println("delivered " + ackTag);
//            },(nackdTag, multiple)-> System.out.println("not delivered!" + nackdTag));
            channel.basicPublish("", QUEUE_NAME, constructQueueProps(initialProps) , msg.getBytes());
            channel.confirmSelect();
            channel.waitForConfirms();

        } catch (TimeoutException | IOException | InterruptedException g) {
            g.printStackTrace();
        }
    }
    static void recogniseAndPublish(Delivery delivery, String name) throws IOException {
        byte[] raw_data  = delivery.getBody();
        File tmp = new File(TMP_PATH + name);
        FileUtils.writeByteArrayToFile(tmp,raw_data);
        try {
            String result= tesseract.doOCR(new File(TMP_PATH+ name));
            publishMessage(result, delivery.getProperties());
        } catch (TesseractException e) {
            System.err.println(e.getMessage());
        }
        FileUtils.deleteQuietly(tmp);
    }
}
