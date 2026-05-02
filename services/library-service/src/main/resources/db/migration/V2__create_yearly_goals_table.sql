CREATE TABLE yearly_goals (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    year INTEGER NOT NULL,
    target_books INTEGER NOT NULL,
    completed_books INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_yearly_goals_user_year UNIQUE (user_id, year),
    CONSTRAINT ck_yearly_goals_target CHECK (target_books > 0),
    CONSTRAINT ck_yearly_goals_completed CHECK (completed_books >= 0),
    CONSTRAINT ck_yearly_goals_status CHECK (status IN ('IN_PROGRESS', 'ACHIEVED', 'MISSED'))
);
