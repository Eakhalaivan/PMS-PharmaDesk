package com.pharmadesk.backend.pharmacy.controller;

import com.pharmadesk.backend.model.Medicine;
import com.pharmadesk.backend.model.MedicineStock;
import com.pharmadesk.backend.pharmacy.repository.MedicineRepository;
import com.pharmadesk.backend.pharmacy.repository.MedicineStockRepository;
import com.pharmadesk.backend.pharmacy.dto.ApiResponse;
import com.pharmadesk.backend.pharmacy.dto.MedicineDTO;
import com.pharmadesk.backend.pharmacy.mapper.MedicineMapper;
import com.pharmadesk.backend.model.StockAdjustment;
import com.pharmadesk.backend.model.PurchaseOrder;
import com.pharmadesk.backend.model.PurchaseOrderItem;
import com.pharmadesk.backend.pharmacy.repository.StockAdjustmentRepository;
import com.pharmadesk.backend.pharmacy.repository.PurchaseOrderRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pharmacy")
public class MedicineController {

    private final MedicineRepository medicineRepository;
    private final MedicineStockRepository stockRepository;
    private final MedicineMapper medicineMapper;
    private final com.pharmadesk.backend.service.EmailService emailService;
    private final com.pharmadesk.backend.repository.UserRepository userRepository;
    private final StockAdjustmentRepository stockAdjustmentRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;

    public MedicineController(MedicineRepository medicineRepository, 
                              MedicineStockRepository stockRepository, 
                              MedicineMapper medicineMapper,
                              com.pharmadesk.backend.service.EmailService emailService,
                              com.pharmadesk.backend.repository.UserRepository userRepository,
                              StockAdjustmentRepository stockAdjustmentRepository,
                              PurchaseOrderRepository purchaseOrderRepository) {
        this.medicineRepository = medicineRepository;
        this.stockRepository = stockRepository;
        this.medicineMapper = medicineMapper;
        this.emailService = emailService;
        this.userRepository = userRepository;
        this.stockAdjustmentRepository = stockAdjustmentRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
    }

    @GetMapping("/medicines")
    public ResponseEntity<ApiResponse<List<MedicineDTO>>> getAllMedicines() {
        List<Medicine> medicines = medicineRepository.findAll();
        List<Object[]> stockSummary = stockRepository.getStockQuantitiesGroupByMedicine();
        java.util.Map<Long, Integer> stockMap = stockSummary.stream()
                .filter(arr -> arr[0] != null && arr[1] != null)
                .collect(java.util.stream.Collectors.toMap(
                        arr -> (Long) arr[0],
                        arr -> ((Number) arr[1]).intValue()
                ));

        List<MedicineDTO> dtos = medicines.stream()
                .map(medicine -> {
                    MedicineDTO dto = medicineMapper.toDto(medicine);
                    dto.setCurrentStock(stockMap.getOrDefault(medicine.getId(), 0));
                    return dto;
                })
                .toList();
        return ResponseEntity.ok(ApiResponse.success(dtos, "Medicines fetched successfully"));
    }

    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','PHARMACY_STAFF')")
    @PostMapping("/medicines")
    public ResponseEntity<ApiResponse<Medicine>> createMedicine(@Valid @RequestBody Medicine medicine) {
        if (medicine.getMedicineCode() == null || medicine.getMedicineCode().trim().isEmpty()) {
            long count = medicineRepository.count();
            medicine.setMedicineCode("MED-" + String.format("%04d", count + 1001));
        }
        Medicine saved = medicineRepository.save(medicine);
        return ResponseEntity.ok(ApiResponse.success(saved, "Medicine added successfully"));
    }

    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','PHARMACY_STAFF')")
    @PutMapping("/medicines/{id}")
    public ResponseEntity<ApiResponse<MedicineDTO>> updateMedicine(@PathVariable Long id, @Valid @RequestBody Medicine medicineData) {
        return medicineRepository.findById(id).map(medicine -> {
            medicine.setName(medicineData.getName());
            medicine.setGenericName(medicineData.getGenericName());
            medicine.setManufacturer(medicineData.getManufacturer());
            medicine.setCategory(medicineData.getCategory());
            medicine.setUnit(medicineData.getUnit());
            medicine.setHsnCode(medicineData.getHsnCode());
            medicine.setTaxPercentage(medicineData.getTaxPercentage());
            medicine.setReorderLevel(medicineData.getReorderLevel());
            medicine.setReorderQuantity(medicineData.getReorderQuantity());
            medicine.setBarcode(medicineData.getBarcode());
            medicine.setSupplierVendor(medicineData.getSupplierVendor());
            medicine.setPackSize(medicineData.getPackSize());
            medicine.setMrp(medicineData.getMrp());
            medicine.setPurchasePrice(medicineData.getPurchasePrice());
            medicine.setSalePrice(medicineData.getSalePrice());
            medicine.setDrugClass(medicineData.getDrugClass());
            medicine.setStorageConditions(medicineData.getStorageConditions());
            medicine.setSchedule(medicineData.getSchedule());
            medicine.setSubstitutes(medicineData.getSubstitutes());
            
            Medicine updated = medicineRepository.save(medicine);
            
            MedicineDTO dto = medicineMapper.toDto(updated);
            dto.setCurrentStock(stockRepository.findByMedicineId(updated.getId()).stream()
                    .mapToInt(MedicineStock::getQuantityAvailable)
                    .sum());
            
            return ResponseEntity.ok(ApiResponse.success(dto, "Medicine updated successfully"));
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/medicines/search")
    public ResponseEntity<List<MedicineDTO>> searchMedicines(@RequestParam String name) {
        List<Medicine> medicines = medicineRepository.findByNameContainingIgnoreCase(name);
        if (medicines.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        List<Long> medicineIds = medicines.stream().map(Medicine::getId).toList();
        List<Object[]> stockSummary = stockRepository.getStockQuantitiesGroupByMedicineIds(medicineIds);
        java.util.Map<Long, Integer> stockMap = stockSummary.stream()
                .filter(arr -> arr[0] != null && arr[1] != null)
                .collect(java.util.stream.Collectors.toMap(
                        arr -> (Long) arr[0],
                        arr -> ((Number) arr[1]).intValue()
                ));

        List<MedicineDTO> dtos = medicines.stream()
                .map(medicine -> {
                    MedicineDTO dto = medicineMapper.toDto(medicine);
                    dto.setCurrentStock(stockMap.getOrDefault(medicine.getId(), 0));
                    return dto;
                })
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/stocks/search")
    public ResponseEntity<List<MedicineStock>> searchStocks(@RequestParam String name) {
        return ResponseEntity.ok(stockRepository.findByMedicineNameContainingIgnoreCaseWithMedicineAndSupplier(name));
    }

    @GetMapping("/stocks/barcode/{barcode}")
    public ResponseEntity<ApiResponse<MedicineStock>> getStockByBarcode(@PathVariable String barcode) {
        return medicineRepository.findByBarcode(barcode)
                .flatMap(medicine -> stockRepository.findByMedicineId(medicine.getId()).stream()
                        .filter(s -> s.getQuantityAvailable() > 0)
                        .findFirst())
                .map(stock -> ResponseEntity.ok(ApiResponse.success(stock, "Stock found")))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/stocks")
    public ResponseEntity<ApiResponse<List<MedicineStock>>> getAllStocks() {
        return ResponseEntity.ok(ApiResponse.success(stockRepository.findAllWithMedicineAndSupplier(), "Stocks fetched successfully"));
    }

    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','PHARMACY_STAFF')")
    @PostMapping("/stocks")
    public ResponseEntity<ApiResponse<MedicineStock>> addStock(@Valid @RequestBody MedicineStock stock) {
        // Ensure medicine is linked
        if (stock.getMedicine() != null && stock.getMedicine().getId() != null) {
            Medicine medicine = medicineRepository.findById(stock.getMedicine().getId())
                    .orElseThrow(() -> new RuntimeException("Medicine not found"));
            stock.setMedicine(medicine);
        }
        MedicineStock saved = stockRepository.save(stock);
        return ResponseEntity.ok(ApiResponse.success(saved, "Stock updated successfully"));
    }

    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','PHARMACY_STAFF')")
    @PostMapping("/stocks/adjust")
    public ResponseEntity<ApiResponse<StockAdjustment>> adjustStock(@Valid @RequestBody StockAdjustment adjustment) {
        MedicineStock stock = stockRepository.findById(adjustment.getMedicineStock().getId())
                .orElseThrow(() -> new RuntimeException("Stock not found"));
        
        stock.setQuantityAvailable(stock.getQuantityAvailable() + adjustment.getAdjustedQuantity());
        stockRepository.save(stock);
        
        // Also ensure medicine is set from stock
        adjustment.setMedicine(stock.getMedicine());
        StockAdjustment saved = stockAdjustmentRepository.save(adjustment);
        return ResponseEntity.ok(ApiResponse.success(saved, "Stock adjusted successfully"));
    }

    @GetMapping("/stocks/valuation")
    public ResponseEntity<ApiResponse<java.util.Map<String, java.math.BigDecimal>>> getStockValuation() {
        List<MedicineStock> stocks = stockRepository.findAll();
        java.math.BigDecimal totalPurchaseValue = java.math.BigDecimal.ZERO;
        java.math.BigDecimal totalMrpValue = java.math.BigDecimal.ZERO;
        java.math.BigDecimal nearExpiryValue = java.math.BigDecimal.ZERO;
        java.math.BigDecimal expiredValue = java.math.BigDecimal.ZERO;

        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate nearExpiryThreshold = today.plusDays(30);

        for (MedicineStock s : stocks) {
            int qty = s.getQuantityAvailable() != null ? s.getQuantityAvailable() : 0;
            if (qty > 0) {
                java.math.BigDecimal pRate = s.getPurchaseRate() != null ? s.getPurchaseRate() : java.math.BigDecimal.ZERO;
                java.math.BigDecimal mRate = s.getSellingRate() != null ? s.getSellingRate() : java.math.BigDecimal.ZERO;
                
                java.math.BigDecimal pVal = pRate.multiply(java.math.BigDecimal.valueOf(qty));
                java.math.BigDecimal mVal = mRate.multiply(java.math.BigDecimal.valueOf(qty));
                
                totalPurchaseValue = totalPurchaseValue.add(pVal);
                totalMrpValue = totalMrpValue.add(mVal);

                if (s.getExpiryDate() != null) {
                    if (s.getExpiryDate().isBefore(today)) {
                        expiredValue = expiredValue.add(pVal);
                    } else if (!s.getExpiryDate().isAfter(nearExpiryThreshold)) {
                        nearExpiryValue = nearExpiryValue.add(pVal);
                    }
                }
            }
        }

        java.util.Map<String, java.math.BigDecimal> valuation = new java.util.HashMap<>();
        valuation.put("totalPurchaseValue", totalPurchaseValue);
        valuation.put("totalMrpValue", totalMrpValue);
        valuation.put("nearExpiryValue", nearExpiryValue);
        valuation.put("expiredValue", expiredValue);

        return ResponseEntity.ok(ApiResponse.success(valuation, "Valuation calculated"));
    }

    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','PHARMACY_STAFF')")
    @PostMapping("/purchase-orders/auto-generate")
    public ResponseEntity<ApiResponse<String>> autoGeneratePOs() {
        List<Medicine> medicines = medicineRepository.findAll();
        List<Object[]> stockSummary = stockRepository.getStockQuantitiesGroupByMedicine();
        java.util.Map<Long, Integer> stockMap = stockSummary.stream()
                .filter(arr -> arr[0] != null && arr[1] != null)
                .collect(java.util.stream.Collectors.toMap(
                        arr -> (Long) arr[0],
                        arr -> ((Number) arr[1]).intValue()
                ));

        java.util.Map<Long, PurchaseOrder> supplierOrders = new java.util.HashMap<>();
        int generatedCount = 0;

        for (Medicine med : medicines) {
            int currentStock = stockMap.getOrDefault(med.getId(), 0);
            if (med.getReorderLevel() != null && currentStock <= med.getReorderLevel()) {
                // Find supplier (from latest stock or medicine's supplierVendor, here assuming basic logic)
                com.pharmadesk.backend.model.Supplier supplier = null;
                List<MedicineStock> latestStocks = stockRepository.findByMedicineId(med.getId());
                if (!latestStocks.isEmpty() && latestStocks.get(0).getSupplier() != null) {
                    supplier = latestStocks.get(0).getSupplier();
                }

                if (supplier != null) {
                    final com.pharmadesk.backend.model.Supplier finalSupplier = supplier;
                    PurchaseOrder po = supplierOrders.computeIfAbsent(supplier.getId(), sid -> {
                        PurchaseOrder newPo = new PurchaseOrder();
                        newPo.setSupplier(finalSupplier);
                        newPo.setStatus("DRAFT");
                        newPo.setPoNumber("PO-" + System.currentTimeMillis() + "-" + sid);
                        return newPo;
                    });

                    PurchaseOrderItem item = new PurchaseOrderItem();
                    item.setMedicine(med);
                    item.setQuantity(med.getReorderQuantity() != null ? med.getReorderQuantity() : 100);
                    item.setEstimatedUnitPrice(med.getPurchasePrice() != null ? med.getPurchasePrice() : java.math.BigDecimal.ZERO);
                    item.setPurchaseOrder(po);
                    po.getItems().add(item);
                    generatedCount++;
                }
            }
        }

        for (PurchaseOrder po : supplierOrders.values()) {
            purchaseOrderRepository.save(po);
        }

        return ResponseEntity.ok(ApiResponse.success("Generated " + supplierOrders.size() + " Purchase Orders covering " + generatedCount + " medicines.", "Auto PO Generation successful"));
    }

    @GetMapping("/stocks/low-stock")
    public ResponseEntity<ApiResponse<List<MedicineDTO>>> getLowStockMedicines() {
        List<Medicine> medicines = medicineRepository.findAll();
        List<Object[]> stockSummary = stockRepository.getStockQuantitiesGroupByMedicine();
        java.util.Map<Long, Integer> stockMap = stockSummary.stream()
                .filter(arr -> arr[0] != null && arr[1] != null)
                .collect(java.util.stream.Collectors.toMap(
                        arr -> (Long) arr[0],
                        arr -> ((Number) arr[1]).intValue()
                ));

        List<MedicineDTO> lowStock = medicines.stream()
                .filter(m -> {
                    int qty = stockMap.getOrDefault(m.getId(), 0);
                    int rlvl = m.getReorderLevel() != null ? m.getReorderLevel() : 0;
                    return rlvl > 0 && qty <= rlvl;
                })
                .map(m -> {
                    MedicineDTO dto = medicineMapper.toDto(m);
                    int qty = stockMap.getOrDefault(m.getId(), 0);
                    dto.setCurrentStock(qty);

                    // Populate frontend-facing alias fields
                    dto.setMedicineName(m.getName());

                    // Resolve supplier from the first non-deleted stock batch for this medicine
                    stockRepository.findByMedicineIdAndDeletedFalse(m.getId())
                        .stream()
                        .filter(s -> s.getSupplier() != null)
                        .findFirst()
                        .ifPresent(s -> {
                            dto.setSupplierVendor(s.getSupplier().getName());
                            dto.setSupplierName(s.getSupplier().getName());
                            dto.setLastUpdated(
                                s.getDateOfEntry() != null
                                    ? s.getDateOfEntry().toString()
                                    : java.time.LocalDate.now().toString()
                            );
                        });

                    // Fallback lastUpdated if no batch resolved a supplier
                    if (dto.getLastUpdated() == null) {
                        dto.setLastUpdated(java.time.LocalDate.now().toString());
                    }

                    return dto;
                })
                .toList();

        return ResponseEntity.ok(ApiResponse.success(lowStock, "Low stock medicines fetched"));
    }
}
