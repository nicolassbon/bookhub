package com.bookhub.catalog.infrastructure.provider.openlibrary.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record OpenLibraryWorkResponse(
        String key,
        String title,
        Description description,
        @JsonProperty("author_name") List<String> authorNames,
        @JsonProperty("isbn_13") List<String> isbn13Values,
        @JsonProperty("first_publish_date") String firstPublishDate,
        @JsonProperty("covers") List<Integer> coverIds) {

    public record Description(String value) {
    }
}
