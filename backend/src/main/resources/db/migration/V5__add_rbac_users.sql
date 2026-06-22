-- Database Migration V5: Add users for RBAC
-- Username: admin_user | Role: ADMIN | Password: password
-- Username: pharma_user | Role: MEDICINE_USER | Password: password
-- Username: billing_user | Role: BILLING_USER | Password: password

INSERT INTO users (username, password_hash, role, name, phone) VALUES 
('admin_user', '$2a$12$R.S/1aWzP/6G.7jGZtMyqef1v5b3S8/HjQ53K4A6P4L8tVbO9k8v6', 'ADMIN', 'System Admin', '9999999999'),
('pharma_user', '$2a$12$R.S/1aWzP/6G.7jGZtMyqef1v5b3S8/HjQ53K4A6P4L8tVbO9k8v6', 'MEDICINE_USER', 'Pharmacy Staff', '8888888888'),
('billing_user', '$2a$12$R.S/1aWzP/6G.7jGZtMyqef1v5b3S8/HjQ53K4A6P4L8tVbO9k8v6', 'BILLING_USER', 'Billing Staff', '7777777777');
