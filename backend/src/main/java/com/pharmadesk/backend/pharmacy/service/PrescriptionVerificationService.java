package com.pharmadesk.backend.pharmacy.service;

import com.pharmadesk.backend.model.Prescription;
import com.pharmadesk.backend.pharmacy.exception.ResourceNotFoundException;
import com.pharmadesk.backend.pharmacy.repository.PrescriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class PrescriptionVerificationService {

    private final PrescriptionRepository prescriptionRepository;

    public PrescriptionVerificationService(PrescriptionRepository prescriptionRepository) {
        this.prescriptionRepository = prescriptionRepository;
    }

    @Transactional
    public Prescription verifyPrescription(Long id, String pharmacistUsername) {
        Prescription p = prescriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription not found"));

        p.setVerificationStatus("VERIFIED");
        p.setVerifiedBy(pharmacistUsername);
        p.setVerifiedAt(LocalDateTime.now());
        return prescriptionRepository.save(p);
    }

    @Transactional
    public Prescription rejectPrescription(Long id, String reason, String pharmacistUsername) {
        Prescription p = prescriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription not found"));

        p.setVerificationStatus("REJECTED");
        p.setVerifiedBy(pharmacistUsername);
        p.setVerifiedAt(LocalDateTime.now());
        p.setStatus("CANCELLED");
        return prescriptionRepository.save(p);
    }
}
