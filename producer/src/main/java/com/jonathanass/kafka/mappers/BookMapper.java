package com.jonathanass.kafka.mappers;

import com.jonathanass.kafka.domain.Book;
import com.jonathanass.kafka.dto.BookDTO;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BookMapper {

    Book toEntity(BookDTO dto);
}
