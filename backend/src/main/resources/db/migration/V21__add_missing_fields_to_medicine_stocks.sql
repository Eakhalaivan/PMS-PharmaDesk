-- Migration V21: Add missing quantity_received and grn_reference columns to medicine_stocks
ALTER TABLE medicine_stocks ADD COLUMN IF NOT EXISTS quantity_received INT NOT NULL DEFAULT 0;
ALTER TABLE medicine_stocks ADD COLUMN IF NOT EXISTS grn_reference VARCHAR(100);
