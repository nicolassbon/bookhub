ALTER TABLE reviews
    ADD CONSTRAINT uk_reviews_user_book UNIQUE (user_id, book_id);
