-- USER ANSWERS
CREATE TABLE IF NOT EXISTS user_answers (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id       BIGINT NOT NULL,
    book_id       VARCHAR(100) NOT NULL,
    week_number   INT NOT NULL,
    lesson_number INT NOT NULL,
    question_id   VARCHAR(200) NOT NULL,
    answer_data   TEXT NOT NULL,
    created_at    TIMESTAMP NOT NULL DEFAULT now(),
    updated_at    TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uq_user_answer UNIQUE (user_id, book_id, question_id)
);

CREATE INDEX IF NOT EXISTS idx_user_answers_lesson
    ON user_answers (user_id, book_id, week_number, lesson_number);
