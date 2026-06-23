package com.pharmadesk.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class SequenceGeneratorService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Transactional
    public String generateNextNumber(String documentType) {
        String selectQuery = "SELECT prefix, last_number, current_year, reset_annually " +
                "FROM document_sequences WHERE document_type = ? FOR UPDATE";
        
        Map<String, Object> sequence = jdbcTemplate.queryForMap(selectQuery, documentType);
        
        String prefix = (String) sequence.get("prefix");
        int lastNumber = (Integer) sequence.get("last_number");
        int sequenceYear = (Integer) sequence.get("current_year");
        boolean resetAnnually = (Boolean) sequence.get("reset_annually");
        
        int currentYear = jdbcTemplate.queryForObject("SELECT YEAR(CURDATE())", Integer.class);
        
        if (resetAnnually && sequenceYear != currentYear) {
            lastNumber = 1;
            sequenceYear = currentYear;
        } else {
            lastNumber += 1;
        }
        
        String updateQuery = "UPDATE document_sequences SET last_number = ?, current_year = ? WHERE document_type = ?";
        jdbcTemplate.update(updateQuery, lastNumber, sequenceYear, documentType);
        
        return String.format("%s-%d-%06d", prefix, sequenceYear, lastNumber);
    }
}
