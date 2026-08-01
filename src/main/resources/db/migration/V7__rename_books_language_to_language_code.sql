-- Rename books.language -> books.language_code
ALTER TABLE books RENAME COLUMN language TO language_code;

-- Keep the index name in sync with the column
ALTER INDEX IF EXISTS idx_books_language RENAME TO idx_books_language_code;
