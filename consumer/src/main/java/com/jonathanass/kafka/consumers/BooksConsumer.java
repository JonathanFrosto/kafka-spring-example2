package com.jonathanass.kafka.consumers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jonathanass.kafka.entities.Book;
import com.jonathanass.kafka.services.BookService;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class BooksConsumer {

    private final BookService bookService;
    private final KafkaTemplate<Integer, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    
    public BooksConsumer(BookService bookService,
                         KafkaTemplate<Integer, String> kafkaTemplate,
                         ObjectMapper objectMapper) {
        this.bookService = bookService;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(groupId = "books-crud", topics = {"books"}, containerFactory = "manualAckListenerCOntainerFactory")
    public void onMessage(String data,  Acknowledgment ack) throws JsonProcessingException {
        System.out.println("===========================");
        System.out.println("Receiving message");
        System.out.println("===========================");

        Book book = objectMapper.readValue(data, Book.class);

        try {
            bookService.saveBook(book);
        } catch (IllegalArgumentException e) {
            sendToDlq(book, ack);
        }
    }

    private void sendToDlq(Book book, Acknowledgment ack) {
        String data = null;

        try {
            data = objectMapper.writeValueAsString(book);
            ListenableFuture<SendResult<Integer, String>> send = kafkaTemplate.send("books-dlq", null, data);
            send.addCallback(callback(data, ack));

        } catch (JsonProcessingException e) {
            fullFailure(ack);
        }
    }

    private void fullFailure(Acknowledgment ack) {
        log.info("Não foi possivel enviar para a dlq");
        ack.acknowledge();
    }

    private ListenableFutureCallback<SendResult<Integer, String>> callback(String data, Acknowledgment ack) {
        return new ListenableFutureCallback<SendResult<Integer, String>>() {

            @Override
            public void onSuccess(SendResult<Integer, String> result) {
                log.info(
                    "Mensagem enviada para dlq no topico: {}, na particao: {} ",
                    result.getProducerRecord().topic(),
                    result.getProducerRecord().partition()
                );
            }

            @Override
            public void onFailure(Throwable ex) {
                fullFailure(ack);
            }
        };
    }
}
