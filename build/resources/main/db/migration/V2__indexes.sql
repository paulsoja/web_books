-- Ускоряем основные выборки
CREATE INDEX IF NOT EXISTS idx_weeks_book ON weeks(book_id);
CREATE INDEX IF NOT EXISTS idx_lessons_week ON lessons(week_id);
CREATE INDEX IF NOT EXISTS idx_blocks_lesson ON lesson_content_blocks(lesson_id, order_index);
CREATE INDEX IF NOT EXISTS idx_hw_lesson ON homeworks(lesson_id);
CREATE INDEX IF NOT EXISTS idx_hw_blocks_hw ON homework_blocks(homework_id, order_index);