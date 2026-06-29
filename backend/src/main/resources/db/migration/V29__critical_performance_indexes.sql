-- Composite index for all bill_date range + is_deleted queries
ALTER TABLE pharmacy_bills 
  ADD INDEX idx_bills_date_deleted (billing_date, is_deleted),
  ADD INDEX idx_bills_status_deleted (payment_status, is_deleted),
  ADD INDEX idx_bills_patient_date (patient_name, billing_date);

-- medicine_stocks: is_deleted filter used constantly
ALTER TABLE medicine_stocks
  ADD INDEX idx_stocks_medicine_deleted (medicine_id, is_deleted, expiry_date),
  ADD INDEX idx_stocks_expiry_deleted (expiry_date, is_deleted, quantity_available);

-- pharmacy_bill_items: FK index
ALTER TABLE pharmacy_bill_items
  ADD INDEX idx_bill_items_bill_id (bill_id);

-- medicine_returns: composite index
ALTER TABLE medicine_returns
  ADD INDEX idx_medicine_returns_status_date (status, created_at);

-- purchase_orders: composite index
ALTER TABLE purchase_orders
  ADD INDEX idx_purchase_orders_status_date (status, created_at);

-- activity_logs: queried by user_id + created_at range
ALTER TABLE activity_logs
  ADD INDEX idx_activity_user_time (user_id, created_at DESC);

-- users: queried by status constantly in StockAlertService
ALTER TABLE users
  ADD INDEX idx_users_status (status, is_deleted);
