CREATE TABLE user_books (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    book_id UUID NOT NULL,
    state VARCHAR(20) NOT NULL DEFAULT 'WANT_TO_READ',
    pages_read INTEGER NOT NULL DEFAULT 0,
    percentage INTEGER NOT NULL DEFAULT 0,
    started_at TIMESTAMPTZ,
    finished_at TIMESTAMPTZ,
    added_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    last_progress_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_user_books_user_book UNIQUE (user_id, book_id),
    CONSTRAINT ck_user_books_state CHECK (state IN ('WANT_TO_READ', 'READING', 'READ')),
    CONSTRAINT ck_user_books_pages_read CHECK (pages_read >= 0),
    CONSTRAINT ck_user_books_percentage CHECK (percentage BETWEEN 0 AND 100)
);

CREATE INDEX idx_user_books_user_id ON user_books(user_id);
CREATE INDEX idx_user_books_user_state ON user_books(user_id, state);
