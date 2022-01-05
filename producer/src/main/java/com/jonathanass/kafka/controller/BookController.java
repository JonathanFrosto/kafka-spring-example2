package com.jonathanass.kafka.controller;

import javax.validation.Valid;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jonathanass.kafka.domain.Book;
import com.jonathanass.kafka.dto.BookDTO;
import com.jonathanass.kafka.mappers.BookMapper;
import com.jonathanass.kafka.producer.BookProducer;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BookController {

    private final BookMapper mapper;
    private final BookProducer producer;
    
    public BookController(BookMapper mapper, BookProducer producer) {
        this.mapper = mapper;
        this.producer = producer;
    }

    @PostMapping("/v1/book")
    public void test(@RequestBody @Valid BookDTO dto) throws JsonProcessingException {
        
        Book book = mapper.toEntity(dto);
        producer.sendEventProduceRecord(book);
    }
}
