package com.pharmadesk.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/system")
public class SystemController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/current-datetime")
    public Map<String, Object> getCurrentDatetime() {
        String query = "SELECT " +
                "DATE_FORMAT(CURDATE(), '%d-%m-%Y') AS current_date_val, " +
                "DATE_FORMAT(CURTIME(), '%H:%i:%s') AS current_time_val, " +
                "DATE_FORMAT(NOW(), '%Y-%m-%dT%T.000Z') AS current_datetime_val, " +
                "DAYNAME(NOW()) AS day_of_week, " +
                "HOUR(NOW()) AS current_hour";

        Map<String, Object> result = jdbcTemplate.queryForMap(query);

        Map<String, Object> response = new HashMap<>();
        response.put("current_date", result.get("current_date_val"));
        response.put("current_time", result.get("current_time_val"));
        response.put("current_datetime", result.get("current_datetime_val"));
        response.put("day_of_week", result.get("day_of_week"));

        int hour = ((Number) result.get("current_hour")).intValue();
        String greeting = "Good Evening";
        if (hour < 12) {
            greeting = "Good Morning";
        } else if (hour < 17) {
            greeting = "Good Afternoon";
        }
        response.put("greeting", greeting);

        // Ideally we fetch the branch name from SecurityContext, using "Main Branch" as default for now
        response.put("branch_name", "Main Branch");

        return response;
    }
}
