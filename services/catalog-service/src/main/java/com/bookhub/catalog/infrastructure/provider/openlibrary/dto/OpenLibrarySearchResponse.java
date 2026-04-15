package com.bookhub.catalog.infrastructure.provider.openlibrary.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record OpenLibrarySearchResponse(List<OpenLibraryBookDoc> docs) {

    public record OpenLibraryBookDoc(
            String key,
            String title,
            @JsonProperty("author_name") List<String> authorNames,
            List<String> isbn,
            @JsonProperty("cover_i") Integer coverId,
            @JsonProperty("first_publish_year") Integer firstPublishYear) {
    }
}
