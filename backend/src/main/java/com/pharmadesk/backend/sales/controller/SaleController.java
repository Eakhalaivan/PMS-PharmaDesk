package com.pharmadesk.backend.sales.controller;

import com.pharmadesk.backend.model.*;
import com.pharmadesk.backend.sales.model.*;

import com.pharmadesk.backend.pharmacy.dto.ApiResponse;
import com.pharmadesk.backend.sales.dto.SaleRequestDTO;
import com.pharmadesk.backend.sales.model.PharmacyBill;
import com.pharmadesk.backend.sales.service.SaleService;
import com.pharmadesk.backend.sales.repository.PharmacyBillRepository;
import com.pharmadesk.backend.pharmacy.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.pharmadesk.backend.sales.service.BillPdfService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@RestController
@RequestMapping("/api/pharmacy/sales")
public class SaleController {

    private final SaleService saleService;
    private final PharmacyBillRepository pharmacyBillRepository;
    private final BillPdfService billPdfService;

    public SaleController(SaleService saleService, PharmacyBillRepository pharmacyBillRepository, BillPdfService billPdfService) {
        this.saleService = saleService;
        this.pharmacyBillRepository = pharmacyBillRepository;
        this.billPdfService = billPdfService;
    }

    @PreAuthorize("hasAnyAuthority('ROLE_SYSTEM_ADMIN','ROLE_BILLING_STAFF','ROLE_SUPERVISOR','ROLE_PHARMACY_STAFF')")
    @PostMapping
    public ResponseEntity<ApiResponse<PharmacyBill>> createSale(@Valid @RequestBody SaleRequestDTO request) {
        PharmacyBill bill = saleService.processSale(request);
        return ResponseEntity.ok(ApiResponse.success(bill, "Sale completed successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<PharmacyBill>>> getAllSales(
            @PageableDefault(size = 20, sort = "billingDate") Pageable pageable) {
        try {
            Page<PharmacyBill> sales = pharmacyBillRepository.findAllWithItemsPaged(pageable);
            return ResponseEntity.ok(ApiResponse.success(sales, "Sales fetched"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error("Error fetching sales: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PharmacyBill>> getSaleById(@PathVariable Long id) {
        PharmacyBill bill = pharmacyBillRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found"));
        return ResponseEntity.ok(ApiResponse.success(bill, "Bill details fetched"));
    }

    @GetMapping("/number/{billNumber}")
    public ResponseEntity<ApiResponse<PharmacyBill>> getSaleByBillNumber(@PathVariable String billNumber) {
        PharmacyBill bill = pharmacyBillRepository.findByBillNumber(billNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found: " + billNumber));
        return ResponseEntity.ok(ApiResponse.success(bill, "Bill fetched"));
    }

    @PreAuthorize("hasAuthority('ROLE_SYSTEM_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSale(@PathVariable Long id) {
        saleService.cancelSale(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Bill cancelled successfully"));
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> getSalePdf(@PathVariable Long id) {
        PharmacyBill bill = pharmacyBillRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found"));
        byte[] pdfBytes = billPdfService.generateBillPdf(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "BILL-" + bill.getBillNumber() + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
}
