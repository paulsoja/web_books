ALTER TABLE books
    ADD COLUMN language VARCHAR(12) NOT NULL DEFAULT 'en';

CREATE INDEX IF NOT EXISTS idx_books_language ON books(language);
