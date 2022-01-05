package com.jonathanass.kafka.repositories;

import com.jonathanass.kafka.entities.Book;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, String> {
    
}
