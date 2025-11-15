-- Таблица книг
CREATE TABLE IF NOT EXISTS books (
  id           BIGINT PRIMARY KEY,               -- из JSON: books[].id (0,1,...)
  number       TEXT NOT NULL,                     -- из JSON: books[].number (строка)
  title        TEXT NOT NULL,
  subtitle     TEXT,
  created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Доп. метаданные книги (author/by_author, how_to_use, before_starting)
CREATE TABLE IF NOT EXISTS book_meta (
  book_id         BIGINT PRIMARY KEY REFERENCES books(id) ON DELETE CASCADE,
  by_author       TEXT,
  how_to_use      TEXT,
  before_starting TEXT
);

-- Недели внутри книги
CREATE TABLE IF NOT EXISTS weeks (
  id          BIGSERIAL PRIMARY KEY,
  book_id     INTEGER NOT NULL REFERENCES books(id) ON DELETE CASCADE,
  number      INTEGER NOT NULL,                   -- weeks[].number
  title       TEXT NOT NULL,
  UNIQUE (book_id, number)
);

-- Уроки внутри недели
CREATE TABLE IF NOT EXISTS lessons (
  id          BIGSERIAL PRIMARY KEY,
  week_id     BIGINT  NOT NULL REFERENCES weeks(id) ON DELETE CASCADE,
  number      INTEGER NOT NULL,                   -- lessons[].number
  title       TEXT NOT NULL,
  quote       TEXT,                               -- lessons[].quote
  UNIQUE (week_id, number)
);

-- Контентные блоки урока (порядок важен)
CREATE TABLE IF NOT EXISTS lesson_content_blocks (
  id            BIGSERIAL PRIMARY KEY,
  lesson_id     BIGINT NOT NULL REFERENCES lessons(id) ON DELETE CASCADE,
  order_index   INTEGER NOT NULL,                 -- позиция в массиве content[]
  block_type    TEXT NOT NULL,      -- 'text' | 'image'
  data_text     TEXT,                             -- для type='text' -> content[].data
  data_image    TEXT,                             -- для type='image' -> content[].data (подпись/путь)
  CHECK (
    (block_type = 'text'  AND data_text  IS NOT NULL AND data_image IS NULL) OR
    (block_type = 'image' AND data_image IS NOT NULL)
  ),
  UNIQUE (lesson_id, order_index)
);

-- Домашнее задание (в JSON одно на урок)
CREATE TABLE IF NOT EXISTS homeworks (
  id          BIGSERIAL PRIMARY KEY,
  lesson_id   BIGINT UNIQUE NOT NULL REFERENCES lessons(id) ON DELETE CASCADE,
  ext_id      INTEGER,                            -- из JSON: home_work.id (если нужно сохранять)
  question    TEXT                                -- home_work.question
);

-- Блоки домашнего задания (радио/чекбокс/текст/текст с пропусками)
CREATE TABLE IF NOT EXISTS homework_blocks (
  id            BIGSERIAL PRIMARY KEY,
  homework_id   BIGINT NOT NULL REFERENCES homeworks(id) ON DELETE CASCADE,
  order_index   INTEGER NOT NULL,                 -- позиция в home_work.block[]
  component     TEXT NOT NULL,      -- 'radio_button' | 'check_box' | 'edit_text' | 'text' | 'text_skip'
  answer_label  TEXT,                             -- поле "answer" (вариант/ярлык/подсказка)
  extra_text    TEXT,                             -- поле "text" для некоторых блоков (например, text_skip с <space>)
  UNIQUE (homework_id, order_index)
);