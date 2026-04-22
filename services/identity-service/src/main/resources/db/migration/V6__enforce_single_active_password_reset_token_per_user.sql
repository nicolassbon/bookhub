DELETE FROM password_reset_tokens
WHERE id IN (
    SELECT id
    FROM (
        SELECT id,
               ROW_NUMBER() OVER (PARTITION BY user_id ORDER BY created_at DESC, id DESC) AS row_number
        FROM password_reset_tokens
    ) ranked_tokens
    WHERE ranked_tokens.row_number > 1
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_password_reset_tokens_user_id
    ON password_reset_tokens (user_id);
