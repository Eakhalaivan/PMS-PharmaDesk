package com.pharmadesk.backend.pharmacy.service;

import com.pharmadesk.backend.model.PurchaseOrder;
import com.pharmadesk.backend.model.PoLineItem;
import com.pharmadesk.backend.pharmacy.repository.PurchaseOrderRepository;
import com.pharmadesk.backend.pharmacy.repository.MedicineRepository;
import com.pharmadesk.backend.pharmacy.repository.SupplierRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class PurchaseOrderService {

    private final PurchaseOrderRepository poRepository;
    private final MedicineRepository medicineRepository;
    private final SupplierRepository supplierRepository;

    public PurchaseOrderService(PurchaseOrderRepository poRepository,
                                MedicineRepository medicineRepository,
                                SupplierRepository supplierRepository) {
        this.poRepository = poRepository;
        this.medicineRepository = medicineRepository;
        this.supplierRepository = supplierRepository;
    }

    public List<PurchaseOrder> getAllPos() {
        return poRepository.findAll();
    }

    public List<PurchaseOrder> getPOsByStatus(String status) {
        return poRepository.findByStatus(status);
    }

    public PurchaseOrder getPoById(String id) {
        return poRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("PO not found: " + id));
    }

    @Transactional
    public PurchaseOrder createPO(PurchaseOrder po) {
        po.setPoId(UUID.randomUUID().toString());
        po.setPoNumber("PO-" + LocalDate.now().toString().replace("-","") + "-" + (System.currentTimeMillis() % 10000));
        po.setStatus("draft");
        if (po.getLineItems() != null) {
            po.getLineItems().forEach(item -> {
                item.setLineId(UUID.randomUUID().toString());
                item.setPurchaseOrder(po);
            });
        }
        return poRepository.save(po);
    }

    @Transactional
    public PurchaseOrder submitForApproval(String poId) {
        PurchaseOrder po = getPoById(poId);
        po.setStatus("submitted");
        po.setSubmittedAt(LocalDateTime.now());
        return poRepository.save(po);
    }

    @Transactional
    public PurchaseOrder approvePO(String poId, Long approvedBy) {
        PurchaseOrder po = getPoById(poId);
        po.setStatus("approved");
        po.setApprovedBy(approvedBy);
        po.setApprovedAt(LocalDateTime.now());
        return poRepository.save(po);
    }

    @Transactional
    public PurchaseOrder sendToSupplier(String poId) {
        PurchaseOrder po = getPoById(poId);
        po.setStatus("sent");
        po.setSentAt(LocalDateTime.now());
        return poRepository.save(po);
    }

    @Transactional
    public PurchaseOrder cancelPO(String poId, String reason, Long user) {
        PurchaseOrder po = getPoById(poId);
        po.setStatus("cancelled");
        po.setCancellationReason(reason);
        po.setCancelledBy(user);
        po.setUpdatedAt(LocalDateTime.now());
        return poRepository.save(po);
    }

    public Map<String, Object> getPoSummary() {
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalOpenPOs", poRepository.countByStatus("sent") + poRepository.countByStatus("submitted"));
        summary.put("awaitingApproval", poRepository.countByStatus("submitted"));
        summary.put("sentToSupplier", poRepository.countByStatus("sent"));
        summary.put("overduePOs", poRepository.findOverduePOs(LocalDate.now()).size());
        return summary;
    }

    public org.springframework.data.domain.Page<PurchaseOrder> getAllPosPaged(
            org.springframework.data.domain.Pageable pageable) {
        return poRepository.findAll(pageable);
    }

    public org.springframework.data.domain.Page<PurchaseOrder> getPOsByStatusPaged(
            String status, org.springframework.data.domain.Pageable pageable) {
        return poRepository.findByStatus(status, pageable);
    }

    public org.springframework.data.domain.Page<PurchaseOrder> searchPOs(
            String term, org.springframework.data.domain.Pageable pageable) {
        return poRepository.findByPoNumberContainingIgnoreCaseOrSupplierNameContainingIgnoreCase(
                term, term, pageable);
    }
}
