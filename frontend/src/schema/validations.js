import { z } from 'zod';

export const loginSchema = z.object({
  username: z.string().min(1, 'Username is required'),
  password: z.string().min(6, 'Password must be at least 6 characters'),
});

export const createSaleSchema = z.object({
  patientName: z.string().optional(),
  doctorName: z.string().min(1, 'Doctor Name is required'),
  billType: z.enum(['CASH', 'CREDIT', 'OTC']),
  paymentMode: z.enum(['CASH', 'CARD', 'UPI']),
  items: z.array(z.object({
    stockId: z.number(),
    quantity: z.number().min(1, 'Quantity must be at least 1'),
    unitPrice: z.number().min(0),
    discountAmount: z.number().optional(),
    taxAmount: z.number().optional()
  })).min(1, 'At least one item is required')
});

export const createMedicineSchema = z.object({
  name: z.string().min(2, 'Medicine name must be at least 2 characters'),
  genericName: z.string().optional(),
  category: z.string().min(1, 'Category is required'),
  manufacturer: z.string().optional(),
  hsnCode: z.string().optional(),
  unit: z.string().min(1, 'Unit is required'),
  reorderLevel: z.number().min(0).optional(),
  taxPercentage: z.number().min(0).max(100).optional(),
  scheduleCategory: z.string().optional()
});

export const createPurchaseOrderSchema = z.object({
  supplierId: z.number().min(1, 'Supplier is required'),
  expectedDate: z.string().min(1, 'Expected date is required'),
  notes: z.string().optional(),
  items: z.array(z.object({
    medicineId: z.number().min(1, 'Medicine is required'),
    quantity: z.number().min(1, 'Quantity must be at least 1'),
    unitPrice: z.number().min(0, 'Unit price must be non-negative')
  })).min(1, 'At least one item is required')
});
