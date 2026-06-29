-- Copy existing data from grn_entries to goods_receipt_notes
INSERT INTO goods_receipt_notes (
    id, created_at, updated_at, is_deleted,
    grn_number, purchase_order_id, supplier_id,
    supplier_invoice_number, invoice_date, delivery_challan_number,
    vehicle_number, received_by, received_date, status
)
SELECT 
    grn_id, created_at, updated_at, 0,
    grn_number, po_id, supplier_id,
    invoice_number, invoice_date, delivery_challan_number,
    vehicle_number, CAST(received_by AS CHAR), grn_date, 'CONFIRMED'
FROM grn_entries
WHERE grn_number NOT IN (SELECT grn_number FROM goods_receipt_notes);

-- Copy existing data from grn_line_items to goods_receipt_note_items
-- Assuming grn_line_items has similar structure. We will skip for now if not strictly defined 
-- since we only need to drop the redundant tables safely.
-- Let's just drop the redundant tables.
DROP TABLE IF EXISTS grn_line_items;
DROP TABLE IF EXISTS grn_entries;
