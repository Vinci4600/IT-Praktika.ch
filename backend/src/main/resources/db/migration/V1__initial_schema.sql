CREATE TABLE users (
  id CHAR(36) NOT NULL PRIMARY KEY,
  email VARCHAR(254) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  role ENUM('student', 'admin') NOT NULL DEFAULT 'student',
  created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE companies (
  id CHAR(36) NOT NULL PRIMARY KEY,
  name VARCHAR(200) NOT NULL,
  location VARCHAR(160) NOT NULL,
  specialties JSON NOT NULL,
  email VARCHAR(254) NULL,
  phone VARCHAR(40) NULL,
  description TEXT NULL,
  website VARCHAR(2048) NULL,
  is_partner BOOLEAN NOT NULL DEFAULT FALSE,
  logo_url VARCHAR(2048) NULL,
  created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  INDEX idx_companies_partner_name (is_partner, name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE internships (
  id CHAR(36) NOT NULL PRIMARY KEY,
  company_id CHAR(36) NULL,
  company_name VARCHAR(200) NOT NULL,
  title VARCHAR(200) NOT NULL,
  location VARCHAR(160) NOT NULL,
  duration VARCHAR(100) NOT NULL,
  start_option ENUM('immediate', 'by_agreement', 'date') NOT NULL DEFAULT 'by_agreement',
  start_date DATE NULL,
  career_field VARCHAR(160) NOT NULL,
  contact_person VARCHAR(200) NULL,
  contact_email VARCHAR(254) NOT NULL,
  contact_phone VARCHAR(40) NULL,
  application_url VARCHAR(2048) NULL,
  compensation VARCHAR(160) NOT NULL DEFAULT 'nicht angegeben',
  description TEXT NOT NULL,
  requirements JSON NOT NULL,
  status ENUM('draft', 'published', 'archived') NOT NULL DEFAULT 'draft',
  created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  CONSTRAINT fk_internships_company
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE SET NULL,
  INDEX idx_internships_status_created (status, created_at),
  INDEX idx_internships_location_field (location, career_field)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE company_contact_requests (
  id CHAR(36) NOT NULL PRIMARY KEY,
  company_name VARCHAR(200) NOT NULL,
  contact_person VARCHAR(200) NOT NULL,
  email VARCHAR(254) NOT NULL,
  phone VARCHAR(40) NOT NULL,
  request_reason VARCHAR(200) NOT NULL,
  preferred_callback_time VARCHAR(100) NOT NULL,
  message TEXT NULL,
  status ENUM('new', 'contacted', 'closed') NOT NULL DEFAULT 'new',
  created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  INDEX idx_company_requests_status_created (status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
