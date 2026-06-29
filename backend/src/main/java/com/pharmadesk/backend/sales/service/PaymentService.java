package com.pharmadesk.backend.sales.service;

import com.pharmadesk.backend.model.*;
import com.pharmadesk.backend.sales.model.*;
import com.pharmadesk.backend.sales.repository.*;

import com.pharmadesk.backend.sales.model.CreditBill;
import com.pharmadesk.backend.sales.model.PharmacyBill;
import com.pharmadesk.backend.pharmacy.enums.PaymentStatus;
import com.pharmadesk.backend.pharmacy.exception.ResourceNotFoundException;
import com.pharmadesk.backend.sales.repository.CreditBillRepository;
import com.pharmadesk.backend.sales.repository.PharmacyBillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class PaymentService {

    private final PharmacyBillRepository pharmacyBillRepository;
    private final CreditBillRepository creditBillRepository;

    public PaymentService(PharmacyBillRepository pharmacyBillRepository, CreditBillRepository creditBillRepository) {
        this.pharmacyBillRepository = pharmacyBillRepository;
        this.creditBillRepository = creditBillRepository;
    }

    @Transactional
    public PharmacyBill applyPartialPayment(Long billId, BigDecimal amount) {
        PharmacyBill bill = pharmacyBillRepository.findById(billId)
                .orElseThrow(() -> new ResourceNotFoundException("Pharmacy bill not found with id: " + billId));

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero");
        }

        BigDecimal newPaidAmount = bill.getPaidAmount().add(amount);
        if (newPaidAmount.compareTo(bill.getNetAmount()) > 0) {
            throw new IllegalArgumentException("Total paid amount cannot exceed net amount");
        }

        BigDecimal newBalance = bill.getNetAmount().subtract(newPaidAmount);
        
        bill.setPaidAmount(newPaidAmount);
        bill.setBalanceAmount(newBalance);

        if (newBalance.compareTo(BigDecimal.ZERO) <= 0) {
            bill.setPaymentStatus(PaymentStatus.PAID);
            bill.setStatus("PAID");
        } else {
            bill.setPaymentStatus(PaymentStatus.PARTIAL);
        }

        PharmacyBill savedBill = pharmacyBillRepository.save(bill);

        Optional<CreditBill> creditBillOpt = creditBillRepository.findByBillId(billId);
        if (creditBillOpt.isPresent()) {
            CreditBill creditBill = creditBillOpt.get();
            creditBill.setPaidAmount(newPaidAmount);
            creditBill.setBalanceAmount(newBalance);
            creditBill.setStatus(savedBill.getPaymentStatus());
            creditBillRepository.save(creditBill);
        }

        return savedBill;
    }
}
