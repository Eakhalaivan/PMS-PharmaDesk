-- V25: Ensure gst_percent column exists on medicines (was in V3 seed data but not entity)
ALTER TABLE medicines
    ADD COLUMN IF NOT EXISTS gst_percent DECIMAL(5,2) DEFAULT 12.00;
