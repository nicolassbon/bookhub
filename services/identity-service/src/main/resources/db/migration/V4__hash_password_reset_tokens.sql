DELETE FROM password_reset_tokens;

ALTER TABLE password_reset_tokens DROP CONSTRAINT IF EXISTS uk_password_reset_tokens_token;

ALTER TABLE password_reset_tokens ADD COLUMN token_hash VARCHAR(64);

ALTER TABLE password_reset_tokens ALTER COLUMN token_hash SET NOT NULL;

ALTER TABLE password_reset_tokens DROP COLUMN token;

CREATE UNIQUE INDEX uk_password_reset_tokens_token_hash
    ON password_reset_tokens (token_hash);
