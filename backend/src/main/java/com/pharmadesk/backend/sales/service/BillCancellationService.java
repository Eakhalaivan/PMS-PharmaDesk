package com.pharmadesk.backend.sales.service;

import com.pharmadesk.backend.pharmacy.exception.ResourceNotFoundException;
import com.pharmadesk.backend.sales.model.BillCancellationRequest;
import com.pharmadesk.backend.sales.repository.BillCancellationRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class BillCancellationService {

    private final BillCancellationRequestRepository repository;
    private final SaleService saleService;

    public BillCancellationService(BillCancellationRequestRepository repository, SaleService saleService) {
        this.repository = repository;
        this.saleService = saleService;
    }

    @Transactional
    public BillCancellationRequest requestCancellation(Long billId, String reason, String requestedBy) {
        BillCancellationRequest request = new BillCancellationRequest();
        request.setBillId(billId);
        request.setRequestedBy(requestedBy);
        request.setRequestedAt(LocalDateTime.now());
        request.setReason(reason);
        request.setStatus("PENDING");
        return repository.save(request);
    }

    @Transactional
    public BillCancellationRequest approveCancellation(Long requestId, String reviewedBy) {
        BillCancellationRequest request = repository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Cancellation request not found"));

        if (!"PENDING".equals(request.getStatus())) {
            throw new IllegalStateException("Request is already " + request.getStatus());
        }

        request.setStatus("APPROVED");
        request.setReviewedBy(reviewedBy);
        request.setReviewedAt(LocalDateTime.now());
        repository.save(request);

        saleService.cancelSale(request.getBillId());

        return request;
    }

    @Transactional
    public BillCancellationRequest rejectCancellation(Long requestId, String reason, String reviewedBy) {
        BillCancellationRequest request = repository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Cancellation request not found"));

        if (!"PENDING".equals(request.getStatus())) {
            throw new IllegalStateException("Request is already " + request.getStatus());
        }

        request.setStatus("REJECTED");
        request.setReviewedBy(reviewedBy);
        request.setReviewedAt(LocalDateTime.now());
        // Append rejection reason if needed, or just set it if we had a field. 
        // We'll append to the reason field for simplicity if there's no rejectionReason field.
        request.setReason(request.getReason() + " | REJECTED: " + reason);
        return repository.save(request);
    }
}
