package com.pharmadesk.backend.pharmacy.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pharmadesk.backend.model.BatchReturnToSupplier;
import com.pharmadesk.backend.model.StockBatch;
import com.pharmadesk.backend.pharmacy.repository.BatchReturnToSupplierRepository;
import com.pharmadesk.backend.pharmacy.repository.StockBatchRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.pharmadesk.backend.pharmacy.dto.common.PageResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ExpiryTrackerService {

    private static final Logger log = LoggerFactory.getLogger(ExpiryTrackerService.class);

    private final StockBatchRepository batchRepository;
    private final BatchReturnToSupplierRepository returnRepository;

    public ExpiryTrackerService(StockBatchRepository batchRepository,
                                 BatchReturnToSupplierRepository returnRepository) {
        this.batchRepository = batchRepository;
        this.returnRepository = returnRepository;
    }

    public PageResponse<StockBatch> getFefoStockView(Pageable pageable) {
        Page<StockBatch> pageResult = batchRepository.findAllOrderByFefo(pageable);
        return new PageResponse<>(pageResult);
    }

    public Map<String, Object> getExpirySummary() {
        LocalDate today = LocalDate.now();
        long activeCount = batchRepository.countActiveBatches();
        long within15Days = batchRepository.countExpiringBatches(today, today.plusDays(15));
        long within30Days = batchRepository.countExpiringBatches(today, today.plusDays(30));
        BigDecimal expiredValue = batchRepository.sumExpiredStockValue(today);
        
        if (expiredValue == null) expiredValue = BigDecimal.ZERO;

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

    public PageResponse<BatchReturnToSupplier> getAllReturns(Pageable pageable) {
        Page<BatchReturnToSupplier> pageResult = returnRepository.findAll(pageable);
        return new PageResponse<>(pageResult);
    }

    @Scheduled(cron = "0 0 0 * * ?") // Midnight daily
    @Transactional
    public void triggerExpiryAlertJob() {
        LocalDate now = LocalDate.now();
        List<StockBatch> expiredBatches = batchRepository.findByExpiryDateBeforeAndExpiredFalse(now);
        for (StockBatch b : expiredBatches) {
            b.setExpired(true);
            batchRepository.save(b);
            log.warn("ALERT: Batch {} has expired.", b.getBatchNumber());
        }
    }
}
