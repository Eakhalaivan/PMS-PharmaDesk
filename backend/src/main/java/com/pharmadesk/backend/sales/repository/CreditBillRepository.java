package com.pharmadesk.backend.sales.repository;

import com.pharmadesk.backend.sales.model.CreditBill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CreditBillRepository extends JpaRepository<CreditBill, Long> {
    Optional<CreditBill> findByBillId(Long billId);
    long countByStatus(com.pharmadesk.backend.pharmacy.enums.PaymentStatus status);
}
