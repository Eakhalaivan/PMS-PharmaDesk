package com.pharmadesk.backend.pharmacy.controller;

import com.pharmadesk.backend.pharmacy.dto.ApiResponse;
import com.pharmadesk.backend.model.Prescription;
import com.pharmadesk.backend.pharmacy.repository.PrescriptionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/pharmacy/prescriptions")
public class PrescriptionController {

    private final PrescriptionRepository prescriptionRepository;

    public PrescriptionController(PrescriptionRepository prescriptionRepository) {
        this.prescriptionRepository = prescriptionRepository;
    }

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<Prescription>>> getPendingPrescriptions() {
        return ResponseEntity.ok(ApiResponse.success(
            prescriptionRepository.findAll().stream()
                .filter(p -> p.getStatus().equals("PENDING"))
                .toList(), 
            "Pending prescriptions fetched"
        ));
    }
}
