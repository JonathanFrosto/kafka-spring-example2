package com.jonathanass.kafka.producer;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jonathanass.kafka.domain.Book;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback ;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class BookProducer {

    private final static String topic = "books";

    @Autowired
    private KafkaTemplate<Integer, String> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;


    public void sendEvent(Book bookEvent) throws JsonProcessingException {
        String data = objectMapper.writeValueAsString(bookEvent);
    
        ListenableFuture<SendResult<Integer, String>> future = kafkaTemplate.send(topic, data);
        future.addCallback(callback(data));
    }

    private ListenableFutureCallback<SendResult<Integer, String>> callback(String data) {
        return new ListenableFutureCallback<SendResult<Integer, String>>() {
    
            @Override
            public void onSuccess(SendResult<Integer, String> result) {
                log.info("Mensagem enviada com sucesso para o topico {} com o valor {}, e com a partition {}",
                         topic, data, result.getRecordMetadata().partition());
            }
    
            @Override
            public void onFailure(Throwable ex) {
                log.error("Error in OnFailure: {}", ex.getMessage());
            }
    
        };
    }

    public void sendEventProduceRecord(Book bookEvent) throws JsonProcessingException {
        String data = objectMapper.writeValueAsString(bookEvent);

        ProducerRecord<Integer, String> record = createProducerRecord(data);
        ListenableFuture<SendResult<Integer, String>> future = kafkaTemplate.send(record);
        future.addCallback(callback(data));
    }

    
    private ProducerRecord<Integer, String> createProducerRecord(String data) {
        List<Header> header = List.of(new RecordHeader("jwt", "ADMINISTRADOR".getBytes()));
        
        return new ProducerRecord<Integer,String>(topic, null, null, data, header);
    }

    public SendResult<Integer, String> sendBookSynchronous(Book bookEvent) throws Exception {

        String value = objectMapper.writeValueAsString(bookEvent);
        SendResult<Integer,String> sendResult = null;
        
        try {
            sendResult = kafkaTemplate.send(topic, value).get(1, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Mandando uma mensagem com a exception {}", e.getMessage());
            throw e;
        }

        return sendResult;
    }
}
