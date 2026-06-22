-- Update passwords to correct BCrypt hash for 'password'
UPDATE users SET password_hash = '$2b$12$dD2IQCyuB6CN1J/MoUUaPe5Pmqa7uPXOOjtbURNANu8UYpyEpmdru' 
WHERE username IN ('admin_user', 'pharma_user', 'billing_user');
