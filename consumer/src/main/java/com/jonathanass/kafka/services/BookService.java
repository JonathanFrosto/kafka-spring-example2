package com.jonathanass.kafka.services;

import com.jonathanass.kafka.entities.Book;

public interface BookService {
    void saveBook(Book book);
}
