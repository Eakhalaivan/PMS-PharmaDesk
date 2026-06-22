package com.pharmadesk.backend.pharmacy.service;

import com.pharmadesk.backend.model.BarcodeScanLog;
import com.pharmadesk.backend.model.Medicine;
import com.pharmadesk.backend.model.StockBatch;
import com.pharmadesk.backend.pharmacy.repository.BarcodeScanLogRepository;
import com.pharmadesk.backend.pharmacy.repository.MedicineRepository;
import com.pharmadesk.backend.pharmacy.repository.StockBatchRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class BarcodeScanService {

    private final MedicineRepository medicineRepository;
    private final StockBatchRepository batchRepository;
    private final BarcodeScanLogRepository logRepository;

    public BarcodeScanService(MedicineRepository medicineRepository,
                              StockBatchRepository batchRepository,
                              BarcodeScanLogRepository logRepository) {
        this.medicineRepository = medicineRepository;
        this.batchRepository = batchRepository;
        this.logRepository = logRepository;
    }

    @Transactional
    public Map<String, Object> resolveScan(String barcodeValue, String scanModule, Long userId) {
        Map<String, Object> result = new HashMap<>();
        BarcodeScanLog log = new BarcodeScanLog();
        log.setScanId(UUID.randomUUID().toString());
        log.setBarcodeValue(barcodeValue);
        log.setScanModule(scanModule);
        log.setScannedBy(userId);
        log.setScannedAt(LocalDateTime.now());

        // 1. Check medicine barcode
        Optional<Medicine> medOpt = medicineRepository.findByBarcode(barcodeValue);
        if (medOpt.isPresent()) {
            Medicine med = medOpt.get();
            result.put("scanType", "medicine_lookup");
            result.put("resolvedEntity", med);
            result.put("scanResult", "success");

            log.setScanType("medicine_lookup");
            log.setResolvedMedicineId(med.getId());
            log.setScanResult("success");
            logRepository.save(log);
            return result;
        }

        // 2. Check batch barcode
        List<StockBatch> batches = batchRepository.findAll();
        for (StockBatch b : batches) {
            if (barcodeValue.equals(b.getBatchNumber())) {
                result.put("scanType", "batch_lookup");
                result.put("resolvedEntity", b);
                result.put("scanResult", "success");

                log.setScanType("batch_lookup");
                log.setResolvedBatchId(b.getBatchId());
                log.setScanResult("success");
                logRepository.save(log);
                return result;
            }
        }

        result.put("scanType", "unknown");
        result.put("scanResult", "not_found");
        log.setScanType("unknown");
        log.setScanResult("not_found");
        logRepository.save(log);

        return result;
    }
}
