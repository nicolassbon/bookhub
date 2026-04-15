package com.bookhub.catalog.web;

import com.bookhub.catalog.application.model.BookSearchItem;
import com.bookhub.catalog.domain.Book;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface BookWebMapper {

    BookSearchResponse toSearchResponse(BookSearchItem item);

    @Mapping(target = "id", expression = "java(book.getId().toString())")
    BookDetailResponse toDetailResponse(Book book);
}
