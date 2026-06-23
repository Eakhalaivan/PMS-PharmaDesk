-- Add last_logout column to users table
ALTER TABLE users ADD COLUMN IF NOT EXISTS last_logout TIMESTAMP;
