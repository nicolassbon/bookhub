ALTER TABLE user_books
    ADD COLUMN book_title VARCHAR(255),
    ADD COLUMN book_cover_url TEXT,
    ADD COLUMN book_page_count INTEGER;

UPDATE user_books
SET book_title = 'Unknown title'
WHERE book_title IS NULL;

ALTER TABLE user_books
    ALTER COLUMN book_title SET NOT NULL,
    ALTER COLUMN percentage DROP NOT NULL;

ALTER TABLE user_books
    DROP CONSTRAINT IF EXISTS ck_user_books_percentage;

ALTER TABLE user_books
    ADD CONSTRAINT ck_user_books_percentage CHECK (percentage IS NULL OR (percentage BETWEEN 0 AND 100));
