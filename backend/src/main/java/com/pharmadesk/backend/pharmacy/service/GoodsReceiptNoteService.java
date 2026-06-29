package com.pharmadesk.backend.pharmacy.service;

import com.pharmadesk.backend.model.*;
import com.pharmadesk.backend.sales.model.*;
import com.pharmadesk.backend.pharmacy.repository.*;
import com.pharmadesk.backend.sales.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.pharmadesk.backend.pharmacy.dto.common.PageResponse;
import java.util.List;

@Service
public class GoodsReceiptNoteService {

    private final GoodsReceiptNoteRepository grnRepository;
    private final PurchaseOrderRepository poRepository;
    private final SupplierRepository supplierRepository;
    private final MedicineStockRepository stockRepository;

    public GoodsReceiptNoteService(GoodsReceiptNoteRepository grnRepository,
                                   PurchaseOrderRepository poRepository,
                                   SupplierRepository supplierRepository,
                                   MedicineStockRepository stockRepository) {
        this.grnRepository = grnRepository;
        this.poRepository = poRepository;
        this.supplierRepository = supplierRepository;
        this.stockRepository = stockRepository;
    }

    public PageResponse<GoodsReceiptNote> getAll(Pageable pageable) {
        Page<GoodsReceiptNote> pageResult = grnRepository.findAll(pageable);
        return new PageResponse<>(pageResult);
    }

    public List<GoodsReceiptNote> getBySupplier(Long supplierId) {
        return grnRepository.findBySupplierId(supplierId);
    }

    public List<GoodsReceiptNote> getByPo(String poId) {
        return grnRepository.findByPurchaseOrderPoId(poId);
    }

    public GoodsReceiptNote getById(Long id) {
        return grnRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("GRN not found: " + id));
    }

    @Transactional
    public GoodsReceiptNote createGrn(GoodsReceiptNote grn) {
        // Auto-generate GRN number
        if (grn.getGrnNumber() == null || grn.getGrnNumber().isEmpty()) {
            String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            grn.setGrnNumber("GRN-" + dateStr + "-" + System.currentTimeMillis() % 10000);
        }

        // Link items to GRN
        if (grn.getItems() != null) {
            grn.getItems().forEach(item -> item.setGoodsReceiptNote(grn));
        }

        return grnRepository.save(grn);
    }

    @Transactional
    public GoodsReceiptNote confirmGrn(Long grnId) {
        GoodsReceiptNote grn = grnRepository.findById(grnId)
                .orElseThrow(() -> new RuntimeException("GRN not found: " + grnId));

        if ("CONFIRMED".equals(grn.getStatus())) {
            throw new RuntimeException("GRN already confirmed");
        }

        // Update stock for each item
        grn.getItems().forEach(item -> {
            int netReceived = (item.getReceivedQuantity() != null ? item.getReceivedQuantity() : 0)
                    - (item.getRejectedQuantity() != null ? item.getRejectedQuantity() : 0);

            if (netReceived > 0 && item.getMedicine() != null) {
                MedicineStock stock = new MedicineStock();
                stock.setMedicine(item.getMedicine());
                stock.setBatchNumber(item.getBatchNumber() != null ? item.getBatchNumber() : "N/A");
                stock.setManufacturingDate(item.getManufacturingDate());
                stock.setExpiryDate(item.getExpiryDate() != null ? item.getExpiryDate() : LocalDate.now().plusYears(2));
                stock.setQuantityReceived(netReceived);
                stock.setQuantityAvailable(netReceived);
                stock.setPurchaseRate(item.getPurchaseRate() != null ? item.getPurchaseRate() : item.getMrp());
                stock.setSellingRate(item.getMrp() != null ? item.getMrp() : BigDecimal.ZERO);
                stock.setSupplier(grn.getSupplier());
                stock.setGrnReference(grn.getGrnNumber());
                stock.setDateOfEntry(LocalDate.now());
                stock.setDeleted(false);
                stockRepository.save(stock);
            }
        });

        // Determine if partial or full delivery
        boolean isPartial = grn.getItems().stream().anyMatch(item ->
                item.getReceivedQuantity() != null && item.getOrderedQuantity() != null
                        && item.getReceivedQuantity() < item.getOrderedQuantity()
        );

        grn.setStatus("CONFIRMED");

        // Update PO status
        if (grn.getPurchaseOrder() != null) {
            PurchaseOrder po = grn.getPurchaseOrder();
            po.setStatus(isPartial ? "PARTIALLY_RECEIVED" : "COMPLETED");
            poRepository.save(po);
        }

        return grnRepository.save(grn);
    }

    @Transactional
    public GoodsReceiptNote updateStatus(Long id, String status) {
        GoodsReceiptNote grn = grnRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("GRN not found: " + id));
        grn.setStatus(status);
        return grnRepository.save(grn);
    }
}
