package com.pharmadesk.backend.sales.repository;

import com.pharmadesk.backend.sales.model.MedicineReturn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MedicineReturnRepository extends JpaRepository<MedicineReturn, Long> {
    @Query("SELECT DISTINCT r FROM MedicineReturn r LEFT JOIN FETCH r.originalBill LEFT JOIN FETCH r.items")
    List<MedicineReturn> findAll();

    List<MedicineReturn> findByOriginalBillId(Long billId);

    List<MedicineReturn> findByReturnDateAfter(LocalDateTime date);
    List<MedicineReturn> findByReturnDateBetween(LocalDateTime start, LocalDateTime end);
    long countByStatus(com.pharmadesk.backend.pharmacy.enums.ReturnStatus status);

    @Query("SELECT SUM(r.totalReturnAmount) FROM MedicineReturn r WHERE r.returnDate BETWEEN :start AND :end AND r.status = :status")
    java.math.BigDecimal sumTotalReturnAmountByDateAndStatus(@org.springframework.data.repository.query.Param("start") LocalDateTime start, 
                                                             @org.springframework.data.repository.query.Param("end") LocalDateTime end, 
                                                             @org.springframework.data.repository.query.Param("status") com.pharmadesk.backend.pharmacy.enums.ReturnStatus status);
}
