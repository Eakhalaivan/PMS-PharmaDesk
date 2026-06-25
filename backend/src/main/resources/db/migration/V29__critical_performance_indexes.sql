-- Composite index for all bill_date range + is_deleted queries
ALTER TABLE sales_bills 
  ADD INDEX idx_bills_date_deleted (bill_date, is_deleted),
  ADD INDEX idx_bills_status_deleted (bill_status, is_deleted),
  ADD INDEX idx_bills_patient_date (patient_name, bill_date);

-- medicine_stocks: is_deleted filter used constantly
ALTER TABLE medicine_stocks
  ADD INDEX idx_stocks_medicine_deleted (medicine_id, is_deleted),
  ADD INDEX idx_stocks_expiry_deleted (expiry_date, is_deleted, quantity_available);

-- activity_logs: queried by user_id + created_at range
ALTER TABLE activity_logs
  ADD INDEX idx_activity_user_time (user_id, created_at DESC);

-- users: queried by status constantly in StockAlertService
ALTER TABLE users
  ADD INDEX idx_users_status (status, is_deleted);
