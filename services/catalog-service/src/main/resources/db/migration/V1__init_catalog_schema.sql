CREATE TABLE books (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author_name VARCHAR(255),
    isbn13 VARCHAR(13),
    source_reference VARCHAR(100) NOT NULL,
    cover_url VARCHAR(500),
    published_year INTEGER,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

ALTER TABLE books
    ADD CONSTRAINT uk_books_source_reference UNIQUE (source_reference);

CREATE INDEX idx_books_title_lower ON books ((LOWER(title)));
CREATE INDEX idx_books_author_name_lower ON books ((LOWER(author_name)));
CREATE INDEX idx_books_isbn13 ON books (isbn13);
