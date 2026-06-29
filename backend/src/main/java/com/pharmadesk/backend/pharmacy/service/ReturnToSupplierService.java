package com.pharmadesk.backend.pharmacy.service;

import com.pharmadesk.backend.model.*;
import com.pharmadesk.backend.sales.model.*;
import com.pharmadesk.backend.pharmacy.repository.*;
import com.pharmadesk.backend.sales.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class ReturnToSupplierService {

    private final ReturnToSupplierRepository returnRepository;
    private final MedicineStockRepository stockRepository;

    public ReturnToSupplierService(ReturnToSupplierRepository returnRepository,
                                   MedicineStockRepository stockRepository) {
        this.returnRepository = returnRepository;
        this.stockRepository = stockRepository;
    }

    public List<ReturnToSupplier> getAll() {
        return returnRepository.findAll();
    }

    public List<ReturnToSupplier> getBySupplier(Long supplierId) {
        return returnRepository.findBySupplierId(supplierId);
    }

    public ReturnToSupplier getById(Long id) {
        return returnRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Return not found: " + id));
    }

    @Transactional
    public ReturnToSupplier createReturn(ReturnToSupplier rts) {
        if (rts.getReturnNumber() == null || rts.getReturnNumber().isEmpty()) {
            rts.setReturnNumber("RTN-" + System.currentTimeMillis() % 100000);
        }

        if (rts.getItems() != null) {
            rts.getItems().forEach(item -> {
                item.setReturnToSupplier(rts);

                // Deduct from stock
                if (item.getMedicine() != null && item.getBatchNumber() != null) {
                    List<MedicineStock> stocks = stockRepository.findByMedicineId(item.getMedicine().getId());
                    stocks.stream()
                            .filter(s -> item.getBatchNumber().equals(s.getBatchNumber()))
                            .findFirst()
                            .ifPresent(s -> {
                                int newQty = Math.max(0, s.getQuantityAvailable() - (item.getReturnQuantity() != null ? item.getReturnQuantity() : 0));
                                s.setQuantityAvailable(newQty);
                                stockRepository.save(s);
                            });
                }
            });
        }

        return returnRepository.save(rts);
    }

    @Transactional
    public ReturnToSupplier updateStatus(Long id, String status, String creditNoteNumber, BigDecimal actualCreditValue) {
        ReturnToSupplier rts = returnRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Return not found: " + id));
        rts.setStatus(status);
        if (creditNoteNumber != null) rts.setCreditNoteNumber(creditNoteNumber);
        if (actualCreditValue != null) rts.setActualCreditValue(actualCreditValue);
        return returnRepository.save(rts);
    }
}
