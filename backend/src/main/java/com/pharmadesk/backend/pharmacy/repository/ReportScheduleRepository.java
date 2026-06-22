package com.pharmadesk.backend.pharmacy.repository;

import com.pharmadesk.backend.model.ReportSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportScheduleRepository extends JpaRepository<ReportSchedule, Long> {
    List<ReportSchedule> findByActiveTrue();
    List<ReportSchedule> findByReportCategory(String category);
}
