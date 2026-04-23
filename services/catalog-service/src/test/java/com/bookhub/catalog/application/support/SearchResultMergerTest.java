package com.bookhub.catalog.application.support;

import static org.assertj.core.api.Assertions.assertThat;

import com.bookhub.catalog.application.model.BookSearchItem;
import java.util.List;
import org.junit.jupiter.api.Test;

class SearchResultMergerTest {

  @Test
  void shouldMergeWithoutCrashingWhenExternalItemHasNullTitle() {
    final BookSearchItem nullTitleItem =
        new BookSearchItem("ext:ol:OL123W", null, "Unknown", "OL123W", null, null, null);

    final List<BookSearchItem> result =
        new SearchResultMerger().merge(List.of(), List.of(nullTitleItem));

    assertThat(result).hasSize(1);
    assertThat(result.getFirst().id()).isEqualTo("ext:ol:OL123W");
    assertThat(result.getFirst().title()).isNull();
  }

  @Test
  void shouldMergeWithoutCrashingWhenExternalItemHasAllNullIdentifiers() {
    final BookSearchItem nullBothItem =
        new BookSearchItem(null, null, "Unknown", null, null, null, null);

    final List<BookSearchItem> result =
        new SearchResultMerger().merge(List.of(), List.of(nullBothItem));

    assertThat(result).hasSize(1);
    assertThat(result.getFirst().id()).isEqualTo("unknown");
  }

  @Test
  void shouldMergeWithoutCrashingWhenExternalItemHasNullSourceReference() {
    final BookSearchItem nullRefItem =
        new BookSearchItem(null, "Some Title", "Author", null, null, null, null);

    final List<BookSearchItem> result =
        new SearchResultMerger().merge(List.of(), List.of(nullRefItem));

    assertThat(result).hasSize(1);
    assertThat(result.getFirst().title()).isEqualTo("Some Title");
  }
}
