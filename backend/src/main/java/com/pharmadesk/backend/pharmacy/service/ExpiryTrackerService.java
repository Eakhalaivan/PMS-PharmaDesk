package com.pharmadesk.backend.pharmacy.service;

import com.pharmadesk.backend.model.BatchReturnToSupplier;
import com.pharmadesk.backend.model.StockBatch;
import com.pharmadesk.backend.pharmacy.repository.BatchReturnToSupplierRepository;
import com.pharmadesk.backend.pharmacy.repository.StockBatchRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ExpiryTrackerService {

    private final StockBatchRepository batchRepository;
    private final BatchReturnToSupplierRepository returnRepository;

    public ExpiryTrackerService(StockBatchRepository batchRepository,
                                 BatchReturnToSupplierRepository returnRepository) {
        this.batchRepository = batchRepository;
        this.returnRepository = returnRepository;
    }

    public List<StockBatch> getFefoStockView() {
        return batchRepository.findAllOrderByFefo();
    }

    public Map<String, Object> getExpirySummary() {
        List<StockBatch> all = batchRepository.findAll();
        long activeCount = all.stream().filter(b -> b.getQuantityAvailable() > 0).count();
        long within15Days = all.stream().filter(b -> {
            long days = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), b.getExpiryDate());
            return days >= 0 && days <= 15 && b.getQuantityAvailable() > 0;
        }).count();
        long within30Days = all.stream().filter(b -> {
            long days = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), b.getExpiryDate());
            return days >= 0 && days <= 30 && b.getQuantityAvailable() > 0;
        }).count();

        BigDecimal expiredValue = all.stream()
                .filter(b -> b.getExpiryDate().isBefore(LocalDate.now()) && b.getQuantityAvailable() > 0)
                .map(b -> b.getPurchasePrice().multiply(BigDecimal.valueOf(b.getQuantityAvailable())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalActiveBatches", activeCount);
        summary.put("expiringWithin15Days", within15Days);
        summary.put("expiringWithin30Days", within30Days);
        summary.put("expiredStockValue", expiredValue);
        return summary;
    }

    @Transactional
    public BatchReturnToSupplier initiateBatchReturn(BatchReturnToSupplier returnRequest) {
        StockBatch batch = batchRepository.findById(returnRequest.getBatchId())
                .orElseThrow(() -> new RuntimeException("Batch not found"));

        if (batch.getQuantityAvailable() < returnRequest.getReturnedQuantity()) {
            throw new IllegalArgumentException("Returned quantity cannot exceed available stock");
        }

        // Deduct from stock batch available quantity
        batch.setQuantityAvailable(batch.getQuantityAvailable() - returnRequest.getReturnedQuantity());
        batchRepository.save(batch);

        returnRequest.setReturnId(java.util.UUID.randomUUID().toString());
        returnRequest.setReturnStatus("initiated");
        returnRequest.setCreatedAt(LocalDateTime.now());
        returnRequest.setUpdatedAt(LocalDateTime.now());
        return repositorySave(returnRequest);
    }

    @Transactional
    public BatchReturnToSupplier repositorySave(BatchReturnToSupplier entity) {
        return returnRepository.save(entity);
    }

    @Transactional
    public BatchReturnToSupplier updateReturnStatus(String returnId, String status) {
        BatchReturnToSupplier ret = returnRepository.findById(returnId)
                .orElseThrow(() -> new RuntimeException("Return record not found"));
        ret.setReturnStatus(status);
        ret.setUpdatedAt(LocalDateTime.now());
        return returnRepository.save(ret);
    }

    public List<BatchReturnToSupplier> getAllReturns() {
        return returnRepository.findAll();
    }

    @Scheduled(cron = "0 0 0 * * ?") // Midnight daily
    @Transactional
    public void triggerExpiryAlertJob() {
        List<StockBatch> all = batchRepository.findAll();
        LocalDate now = LocalDate.now();
        for (StockBatch b : all) {
            if (b.getExpiryDate().isBefore(now) && !b.isExpired()) {
                b.setExpired(true);
                batchRepository.save(b);
                System.out.println("ALERT: Batch " + b.getBatchNumber() + " has expired.");
            }
        }
    }
}
