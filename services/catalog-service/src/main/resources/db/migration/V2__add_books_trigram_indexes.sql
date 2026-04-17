CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX IF NOT EXISTS idx_books_title_trgm
    ON books USING GIN (title gin_trgm_ops);

CREATE INDEX IF NOT EXISTS idx_books_author_name_trgm
    ON books USING GIN (author_name gin_trgm_ops);
