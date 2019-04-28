package com.cdocs.cdocsuiservice.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.rabbitmq.client.*;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class DocHandler implements AutoCloseable{

    private Connection connection;
    private Channel channel;
    private boolean fetched = false;
    /**
     *
     * @param props to set filename, content type to be accessed by the consumer
     * @return
     */
    private AMQP.BasicProperties constructQueueProps(Map<String,Object> props){
        String doc_uuid = UUID.randomUUID().toString();
        System.out.println("Before Publish DOC_ID " +  doc_uuid);
        return new AMQP.BasicProperties.Builder()
                .replyTo(CONSUMING_QUEUE)
                .headers(props)
                .correlationId(doc_uuid)
                .build();
    }


    private void updateModel(String processed_data, Model model){
        JSONObject parsedData = new JSONObject(processed_data);
        model.addAttribute("nouns",parsedData.get("nouns"));
        model.addAttribute("verbs",parsedData.get("verbs"));
        java.lang.reflect.Type mapType = new TypeToken<Map<String, String>>(){}.getType();
        Gson gson = new Gson();
        Map<String, Object> entitiesMap = gson.fromJson(parsedData.getJSONObject("entities").toString(), mapType );
        model.addAttribute("entities",entitiesMap);
    }

    private void listenForResponseAndUpdateModel(Model model, BasicProperties initial_properties) throws IOException, TimeoutException, InterruptedException {

        final String[] corrId = {""};
        final String[] processedData = {""};

        channel.queueDeclare(CONSUMING_QUEUE,false,false,false,null);
        String ctag= channel.basicConsume(CONSUMING_QUEUE, false, (consumerTag, delivery) -> {
            corrId[0] = new String(delivery.getProperties().getCorrelationId());
            System.out.println("initial props " + initial_properties.getCorrelationId());
            if (initial_properties.getCorrelationId().equals(corrId[0])) {
                System.out.println("After consume of doc id !" + corrId[0]);
                processedData[0] = new String(delivery.getBody(),"UTF-8");
                System.out.println("Content----" + processedData[0]);
                updateModel(processedData[0],model);
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(),false);
                fetched = true;
            }
        }, consumerTag -> {
        });
        channel.basicCancel(ctag);

    }

    private Channel getChannel() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        connection = factory.newConnection();
        channel = connection.createChannel();
        return channel;
    }
    @Value("${PUBLISHING_QUEUE}")
    private String PUBLISHING_QUEUE_NAME;
    @Value("${CONSUMING_QUEUE}")
    private String CONSUMING_QUEUE;
    @RequestMapping("/uploaded")
    public String handleDoc(Model model, @RequestParam("doc") MultipartFile file) throws IOException, TimeoutException, InterruptedException {
        //setup headers for publishing
        fetched = false;
        Map<String,Object> headers = new HashMap<>();
        headers.put("DOC_NAME",file.getOriginalFilename());
        Channel channel = getChannel();
        channel.queueDeclare(PUBLISHING_QUEUE_NAME, false, false, false, null);
        //publish raw document to the queue
        AMQP.BasicProperties properties = constructQueueProps(headers);
        channel.basicPublish("", PUBLISHING_QUEUE_NAME, properties, file.getBytes());
        channel.confirmSelect();
        channel.waitForConfirms();
        //consume processed doc from queue
        System.out.println("Waiting for processed doc...");
        while(!fetched){
            listenForResponseAndUpdateModel(model,properties);
        }
        return "Result";
    }

    @Override
    public void close() throws Exception {
        connection.close();
    }
}
