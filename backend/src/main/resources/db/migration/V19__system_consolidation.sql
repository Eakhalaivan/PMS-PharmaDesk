-- Migration V19: Database schema consolidation and additions for real data operations

-- 1. Ensure medicines has all requested fields
ALTER TABLE medicines ADD COLUMN IF NOT EXISTS drug_class VARCHAR(100);
ALTER TABLE medicines ADD COLUMN IF NOT EXISTS schedule_type VARCHAR(50);
ALTER TABLE medicines ADD COLUMN IF NOT EXISTS storage_condition VARCHAR(255);
ALTER TABLE medicines ADD COLUMN IF NOT EXISTS is_returnable BOOLEAN DEFAULT TRUE;
ALTER TABLE medicines ADD COLUMN IF NOT EXISTS pack_size VARCHAR(50);
ALTER TABLE medicines ADD COLUMN IF NOT EXISTS mrp_per_unit DECIMAL(19, 2);
ALTER TABLE medicines ADD COLUMN IF NOT EXISTS purchase_price DECIMAL(19, 2);
ALTER TABLE medicines ADD COLUMN IF NOT EXISTS sale_price DECIMAL(19, 2);

-- 2. Ensure medicine_stocks has batch specific data
ALTER TABLE medicine_stocks ADD COLUMN IF NOT EXISTS manufacturing_date DATE;
ALTER TABLE medicine_stocks ADD COLUMN IF NOT EXISTS grn_id BIGINT;
ALTER TABLE medicine_stocks ADD COLUMN IF NOT EXISTS is_quarantined BOOLEAN DEFAULT FALSE;

-- 3. Ensure suppliers has all profile details
ALTER TABLE suppliers ADD COLUMN IF NOT EXISTS supplier_code VARCHAR(50);
ALTER TABLE suppliers ADD COLUMN IF NOT EXISTS supplier_type VARCHAR(50);
ALTER TABLE suppliers ADD COLUMN IF NOT EXISTS contact_person VARCHAR(100);
ALTER TABLE suppliers ADD COLUMN IF NOT EXISTS mobile VARCHAR(20);
ALTER TABLE suppliers ADD COLUMN IF NOT EXISTS email VARCHAR(100);
ALTER TABLE suppliers ADD COLUMN IF NOT EXISTS city VARCHAR(100);
ALTER TABLE suppliers ADD COLUMN IF NOT EXISTS state VARCHAR(100);
ALTER TABLE suppliers ADD COLUMN IF NOT EXISTS pincode VARCHAR(20);
ALTER TABLE suppliers ADD COLUMN IF NOT EXISTS drug_license_number VARCHAR(100);
ALTER TABLE suppliers ADD COLUMN IF NOT EXISTS drug_license_expiry DATE;
ALTER TABLE suppliers ADD COLUMN IF NOT EXISTS pan_number VARCHAR(20);
ALTER TABLE suppliers ADD COLUMN IF NOT EXISTS bank_account VARCHAR(100);
ALTER TABLE suppliers ADD COLUMN IF NOT EXISTS ifsc_code VARCHAR(20);
ALTER TABLE suppliers ADD COLUMN IF NOT EXISTS payment_terms_days INT DEFAULT 30;
ALTER TABLE suppliers ADD COLUMN IF NOT EXISTS credit_limit DECIMAL(19, 2) DEFAULT 100000.00;
ALTER TABLE suppliers ADD COLUMN IF NOT EXISTS average_lead_time INT DEFAULT 3;
ALTER TABLE suppliers ADD COLUMN IF NOT EXISTS performance_score DECIMAL(5, 2) DEFAULT 100.00;
ALTER TABLE suppliers ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'ACTIVE';

-- 4. Create Narcotic Register Table if not exists
CREATE TABLE IF NOT EXISTS narcotic_register (
    entry_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    serial_number VARCHAR(100) NOT NULL UNIQUE,
    entry_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    medicine_id BIGINT NOT NULL,
    batch_id BIGINT NOT NULL,
    patient_full_name VARCHAR(255) NOT NULL,
    patient_age INT,
    patient_gender VARCHAR(20),
    patient_id BIGINT,
    ward_name VARCHAR(100),
    bed_number VARCHAR(50),
    doctor_name VARCHAR(255),
    doctor_registration_number VARCHAR(100),
    prescription_number VARCHAR(100),
    prescription_date DATE,
    quantity_prescribed INT NOT NULL,
    quantity_dispensed INT NOT NULL,
    opening_balance INT NOT NULL,
    closing_balance INT NOT NULL,
    dispensing_pharmacist_id BIGINT,
    digital_acknowledgment_timestamp TIMESTAMP,
    remarks TEXT,
    discrepancy_flag BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (medicine_id) REFERENCES medicines(id),
    FOREIGN KEY (batch_id) REFERENCES medicine_stocks(id),
    FOREIGN KEY (patient_id) REFERENCES patients(id)
);
