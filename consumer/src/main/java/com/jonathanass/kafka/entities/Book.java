package com.jonathanass.kafka.entities;

import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Data;

@Entity
@Data
public class Book {
    
    @Id
    private String isbn;
    private String nome;
}
