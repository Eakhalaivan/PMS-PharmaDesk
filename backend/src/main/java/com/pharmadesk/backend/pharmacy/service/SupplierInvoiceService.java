package com.pharmadesk.backend.pharmacy.service;

import com.pharmadesk.backend.model.*;
import com.pharmadesk.backend.sales.model.*;
import com.pharmadesk.backend.pharmacy.repository.*;
import com.pharmadesk.backend.sales.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class SupplierInvoiceService {

    private final SupplierInvoiceRepository invoiceRepository;
    private final GoodsReceiptNoteRepository grnRepository;
    private final PurchaseOrderRepository poRepository;

    public SupplierInvoiceService(SupplierInvoiceRepository invoiceRepository,
                                   GoodsReceiptNoteRepository grnRepository,
                                   PurchaseOrderRepository poRepository) {
        this.invoiceRepository = invoiceRepository;
        this.grnRepository = grnRepository;
        this.poRepository = poRepository;
    }

    public List<SupplierInvoice> getAll() {
        return invoiceRepository.findAll();
    }

    public List<SupplierInvoice> getBySupplier(Long supplierId) {
        return invoiceRepository.findBySupplierId(supplierId);
    }

    public SupplierInvoice getById(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found: " + id));
    }

    @Transactional
    public SupplierInvoice createInvoice(SupplierInvoice invoice) {
        if (invoice.getItems() != null) {
            invoice.getItems().forEach(item -> item.setSupplierInvoice(invoice));
        }
        return invoiceRepository.save(invoice);
    }

    /**
     * Perform 3-way matching: PO vs GRN vs Invoice.
     * Flags discrepancies and updates item severity.
     */
    @Transactional
    public SupplierInvoice performMatching(Long invoiceId) {
        SupplierInvoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found: " + invoiceId));

        GoodsReceiptNote grn = invoice.getGoodsReceiptNote();
        PurchaseOrder po = invoice.getPurchaseOrder();

        boolean hasBlocker = false;
        boolean hasWarning = false;

        // Check GSTIN on invoice vs supplier GSTIN
        if (invoice.getSupplier() != null
                && invoice.getGstinOnInvoice() != null
                && !invoice.getGstinOnInvoice().equalsIgnoreCase(invoice.getSupplier().getGstin())) {
            invoice.setMatchNotes((invoice.getMatchNotes() != null ? invoice.getMatchNotes() + "\n" : "")
                    + "GSTIN_MISMATCH: Invoice GSTIN does not match supplier master.");
            hasBlocker = true;
        }

        if (invoice.getItems() != null && grn != null && po != null) {
            for (SupplierInvoiceItem item : invoice.getItems()) {
                String discrepancy = null;
                String severity = null;

                // Find corresponding GRN item
                GoodsReceiptNoteItem grnItem = grn.getItems().stream()
                        .filter(g -> g.getMedicine() != null && item.getMedicine() != null
                                && g.getMedicine().getId().equals(item.getMedicine().getId()))
                        .findFirst().orElse(null);

                // Find corresponding PO item
                PurchaseOrderItem poItem = po.getItems().stream()
                        .filter(p -> p.getMedicine() != null && item.getMedicine() != null
                                && p.getMedicine().getId().equals(item.getMedicine().getId()))
                        .findFirst().orElse(null);

                if (grnItem != null && item.getBilledQuantity() != null) {
                    int grnQty = grnItem.getReceivedQuantity() != null ? grnItem.getReceivedQuantity() : 0;
                    // Overbilling check
                    if (item.getBilledQuantity() > grnQty) {
                        discrepancy = "QTY_OVERBILLED";
                        severity = "BLOCK";
                        hasBlocker = true;
                    }
                }

                if (poItem != null && item.getBilledPrice() != null) {
                    BigDecimal agreedPrice = poItem.getNegotiatedPrice() != null
                            ? poItem.getNegotiatedPrice() : poItem.getEstimatedUnitPrice();
                    if (agreedPrice != null && item.getBilledPrice().compareTo(agreedPrice) > 0) {
                        if (discrepancy == null) {
                            discrepancy = "PRICE_VARIANCE";
                            severity = "WARNING";
                            hasWarning = true;
                        }
                    }

                    // GST check
                    if (poItem.getGstPercentage() != null && item.getGstPercentage() != null
                            && !poItem.getGstPercentage().equals(item.getGstPercentage())) {
                        if (discrepancy == null) {
                            discrepancy = "GST_MISMATCH";
                            severity = "BLOCK";
                            hasBlocker = true;
                        }
                    }
                }

                item.setDiscrepancyType(discrepancy != null ? discrepancy : "NONE");
                item.setDiscrepancySeverity(severity);
            }
        }

        // Set overall invoice match status
        if (!hasBlocker && !hasWarning) {
            invoice.setStatus("MATCHED");
        } else if (hasBlocker) {
            invoice.setStatus("DISPUTED");
        }

        return invoiceRepository.save(invoice);
    }

    @Transactional
    public SupplierInvoice updateStatus(Long id, String status) {
        SupplierInvoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found: " + id));
        invoice.setStatus(status);
        return invoiceRepository.save(invoice);
    }

    public BigDecimal getOutstandingBalance(Long supplierId) {
        return invoiceRepository.sumOutstandingBySupplierId(supplierId);
    }
}
