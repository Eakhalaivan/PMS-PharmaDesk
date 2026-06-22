package com.pharmadesk.backend.pharmacy.controller;

import com.pharmadesk.backend.pharmacy.dto.ApiResponse;
import com.pharmadesk.backend.pharmacy.service.BarcodeScanService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/barcode")
public class BarcodeController {

    private final BarcodeScanService service;

    public BarcodeController(BarcodeScanService service) {
        this.service = service;
    }

    @PostMapping("/scan")
    public ResponseEntity<ApiResponse<Map<String, Object>>> scan(
            @RequestBody Map<String, String> body, @RequestParam Long userId) {
        String barcodeValue = body.get("barcodeValue");
        String scanModule = body.get("scanModule");
        if (barcodeValue == null || barcodeValue.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Barcode value is required"));
        }
        return ResponseEntity.ok(ApiResponse.success(service.resolveScan(barcodeValue, scanModule, userId), "Scan resolved"));
    }
}
