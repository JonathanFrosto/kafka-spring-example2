package com.jonathanass.kafka.consumers;

import java.util.concurrent.ExecutionException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jonathanass.kafka.entities.Book;

import com.jonathanass.kafka.repositories.BookRepository;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.support.SendResult;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.context.TestPropertySource;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(partitions = 3, topics = {"books"})
@TestPropertySource(properties = {
    "spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}",
    "spring.kafka.consumer.bootstrap-servers=${spring.embedded.kafka.brokers}",
})
public class BookConsumerTest {
    
    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    @Autowired
    private KafkaTemplate<Integer, String> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private KafkaListenerEndpointRegistry endpointRegistry;

    @Autowired
    private BookRepository bookRepository;

    @BeforeEach
    void setUp() {
        for (MessageListenerContainer messageListenerContainer : endpointRegistry.getAllListenerContainers()) {
            ContainerTestUtils.waitForAssignment(messageListenerContainer, embeddedKafka.getPartitionsPerTopic());
        }
    }

    @Test
    @DisplayName("Send valid message")
    void validRecord() throws JsonProcessingException, InterruptedException, ExecutionException {
        // Given
        Book book = new Book();
        book.setIsbn("1");
        book.setNome("Anjos e demonios");

        String json = objectMapper.writeValueAsString(book);

        // When 
        SendResult<Integer, String> result = kafkaTemplate.send("books", json).get();
        printSuccess(result.getProducerRecord());

        // Then
        Assertions.assertEquals(1, bookRepository.findAll().size());
    }

    @Test
    @DisplayName("Send invalid message")
    void invalidRecord() throws JsonProcessingException, InterruptedException, ExecutionException {
        // Given
        Book book = new Book();
        book.setNome("Anjos e demonios");

        String json = objectMapper.writeValueAsString(book);

        // When
        SendResult<Integer, String> result = kafkaTemplate.send("books", json).get();
        printSuccess(result.getProducerRecord());

        // Then
        verify(kafkaTemplate, times(1)).send("books-dlq", null, anyString());
    }

    private void printSuccess(ProducerRecord<Integer, String> producerRecord) {
        System.out.println("=========================");
        System.out.println("MENSAGEM ENVIADA");
        System.out.println("TOPICO: " + producerRecord.topic());
        System.out.println("DATA: " + producerRecord.value());
        System.out.println("PARTICAO: " + producerRecord.partition());
        System.out.println("=========================");
    }
}
