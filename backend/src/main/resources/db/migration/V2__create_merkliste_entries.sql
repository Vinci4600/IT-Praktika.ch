CREATE TABLE merkliste_entries (
  id CHAR(36) NOT NULL PRIMARY KEY,
  user_id CHAR(36) NOT NULL,
  internship_id CHAR(36) NOT NULL,
  created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  CONSTRAINT uq_merkliste_user_internship UNIQUE (user_id, internship_id),
  CONSTRAINT fk_merkliste_user
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_merkliste_internship
    FOREIGN KEY (internship_id) REFERENCES internships(id) ON DELETE CASCADE,
  INDEX idx_merkliste_user_created (user_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
