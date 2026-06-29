package com.pharmadesk.backend.sales.repository;

import com.pharmadesk.backend.sales.model.BillCancellationRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BillCancellationRequestRepository extends JpaRepository<BillCancellationRequest, Long> {
}
