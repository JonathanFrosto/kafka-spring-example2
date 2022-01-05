package com.jonathanass.kafka.dto;

import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class BookDTO {

    private String isbn;

    @NotNull
    private String name;
}
