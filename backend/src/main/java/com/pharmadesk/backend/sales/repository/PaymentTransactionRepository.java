package com.pharmadesk.backend.sales.repository;

import com.pharmadesk.backend.sales.model.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
    List<PaymentTransaction> findByCreditBillId(Long creditBillId);
}
