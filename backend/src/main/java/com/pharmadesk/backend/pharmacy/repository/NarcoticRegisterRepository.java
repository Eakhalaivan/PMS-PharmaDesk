package com.pharmadesk.backend.pharmacy.repository;

import com.pharmadesk.backend.model.NarcoticRegister;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NarcoticRegisterRepository extends JpaRepository<NarcoticRegister, Long> {

    List<NarcoticRegister> findByMedicineIdAndEntryDateBetween(Long medicineId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT nr FROM NarcoticRegister nr WHERE nr.medicine.id = :medicineId ORDER BY nr.entryDate DESC, nr.entryId DESC")
    List<NarcoticRegister> findLatestByMedicineId(@Param("medicineId") Long medicineId);

    default Optional<NarcoticRegister> findLatestEntry(Long medicineId) {
        List<NarcoticRegister> list = findLatestByMedicineId(medicineId);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }
}
