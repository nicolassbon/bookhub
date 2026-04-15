package com.bookhub.catalog.application.support;

import com.bookhub.catalog.application.model.BookSearchItem;
import com.bookhub.catalog.domain.Book;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class SearchResultMerger {

    private static final String EXTERNAL_PREFIX = "ext:ol:";

    public List<BookSearchItem> merge(final List<Book> localBooks, final List<BookSearchItem> externalBooks) {
        final Map<String, BookSearchItem> merged = new LinkedHashMap<>();

        for (Book localBook : localBooks) {
            final BookSearchItem localItem = toLocalItem(localBook);
            merged.put(mergeKey(localItem), localItem);
        }

        for (BookSearchItem externalBook : externalBooks) {
            final BookSearchItem ephemeralExternal = toEphemeralExternalItem(externalBook);
            final String key = mergeKey(ephemeralExternal);
            merged.putIfAbsent(key, ephemeralExternal);
        }

        return List.copyOf(merged.values());
    }

    private BookSearchItem toLocalItem(final Book localBook) {
        return new BookSearchItem(
                localBook.getId().toString(),
                localBook.getTitle(),
                localBook.getAuthorName(),
                BookNormalization.normalizeSourceReference(localBook.getSourceReference()),
                BookNormalization.normalizeIsbn13(localBook.getIsbn13()),
                localBook.getCoverUrl(),
                localBook.getPublishedYear());
    }

    private BookSearchItem toEphemeralExternalItem(final BookSearchItem external) {
        final String normalizedSourceReference = BookNormalization.normalizeSourceReference(external.sourceReference());
        return new BookSearchItem(
                EXTERNAL_PREFIX + normalizedSourceReference,
                external.title(),
                external.authorName(),
                normalizedSourceReference,
                BookNormalization.normalizeIsbn13(external.isbn13()),
                external.coverUrl(),
                external.publishedYear());
    }

    private String mergeKey(final BookSearchItem item) {
        if (item.sourceReference() != null) {
            return "source:" + item.sourceReference().toLowerCase(Locale.ROOT);
        }
        if (item.isbn13() != null) {
            return "isbn:" + item.isbn13();
        }
        return "title:" + item.title().toLowerCase(Locale.ROOT);
    }
}
