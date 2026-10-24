CREATE TABLE authentication_rate_limits (
  rate_key CHAR(64) NOT NULL PRIMARY KEY,
  attempts INT NOT NULL,
  window_start TIMESTAMP(3) NOT NULL,
  expires_at TIMESTAMP(3) NOT NULL,
  INDEX idx_auth_rate_limits_expiry (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
