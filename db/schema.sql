CREATE TABLE transactions (
  id SERIAL PRIMARY KEY,
  type VARCHAR(20) NOT NULL,
  amount NUMERIC(12,2) NOT NULL,
  category VARCHAR(100),
  description TEXT,
  date DATE
);
