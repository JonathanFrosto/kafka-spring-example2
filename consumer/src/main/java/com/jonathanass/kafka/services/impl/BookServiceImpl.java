package com.jonathanass.kafka.services.impl;

import com.jonathanass.kafka.entities.Book;
import com.jonathanass.kafka.repositories.BookRepository;
import com.jonathanass.kafka.services.BookService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;

    public BookServiceImpl(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    public void saveBook(Book book) {

        if (book.getIsbn() == null) {
            throw new IllegalArgumentException("Isbn não pode ser nulo");
        }

        try {
            bookRepository.save(book);
            log.info("Success saving the book");
        } catch (Exception e) {
            log.info("Failed to save book");
        }

    }
    
}
