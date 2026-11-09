DROP TABLE IF EXISTS agent_log;
DROP TABLE IF EXISTS puzzles;
DROP TABLE IF EXISTS blocked_words;
DROP TABLE IF EXISTS words;
DROP TABLE IF EXISTS themes;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
  id INT NOT NULL AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_users_username (username)
) ENGINE=InnoDB;

CREATE TABLE themes (
  id INT NOT NULL AUTO_INCREMENT,
  name VARCHAR(50) NOT NULL,
  holiday_date DATE NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uq_theme_name (name)
) ENGINE=InnoDB;

CREATE TABLE words (
  id INT NOT NULL AUTO_INCREMENT,
  word CHAR(5) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uq_words_word (word)
) ENGINE=InnoDB;

CREATE TABLE blocked_words (
  id INT NOT NULL AUTO_INCREMENT,
  word CHAR(5) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uq_blocked_word (word)
) ENGINE=InnoDB;

CREATE TABLE puzzles (
  id INT NOT NULL AUTO_INCREMENT,
  start_word_id INT NOT NULL,
  end_word_id INT NOT NULL,
  theme_id INT NOT NULL,
  user_id INT NULL,
  difficulty_score INT NOT NULL,
  ladder_length INT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  last_used_date DATE NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_puzzles_start FOREIGN KEY (start_word_id) REFERENCES words (id),
  CONSTRAINT fk_puzzles_end   FOREIGN KEY (end_word_id)   REFERENCES words (id),
  CONSTRAINT fk_puzzles_theme FOREIGN KEY (theme_id)      REFERENCES themes (id),
  CONSTRAINT fk_puzzles_user  FOREIGN KEY (user_id)       REFERENCES users (id)
    ON DELETE SET NULL,
  CONSTRAINT chk_words_differ CHECK (start_word_id <> end_word_id)
) ENGINE=InnoDB;

CREATE TABLE agent_log (
  log_id INT NOT NULL AUTO_INCREMENT,
  user_id INT NULL,
  tool_name VARCHAR(50) NOT NULL,
  input TEXT NULL,
  output TEXT NULL,
  logged_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (log_id),
  INDEX idx_agent_log_user (user_id),
  INDEX idx_agent_log_time (logged_at),
  CONSTRAINT fk_agent_log_user FOREIGN KEY (user_id) REFERENCES users (id)
    ON DELETE SET NULL
) ENGINE=InnoDB;
INSERT INTO users (username, password_hash)
VALUES ('demo', 'REPLACE_WITH_REAL_HASH_LAtER');

INSERT INTO themes (name, holiday_date)
VALUES ('Halloween', '2026-10-31'),
       ('Thanksgiving', '2026-11-26');

INSERT INTO words (word)
VALUES ('GHOST'), ('WITCH'), ('CANDY'), ('LIMES'), ('FEAST');

INSERT INTO blocked_words (word)
VALUES ('XXXXX'), ('TESTB');

INSERT INTO puzzles (start_word_id, end_word_id, theme_id, user_id, difficulty_score, ladder_length)
VALUES (
  (SELECT id FROM words WHERE word = 'GHOST'),
  (SELECT id FROM words WHERE word = 'WITCH'),
  (SELECT id FROM themes WHERE name = 'Halloween'),
  (SELECT id FROM users WHERE username = 'demo'),
  70, 6
);
