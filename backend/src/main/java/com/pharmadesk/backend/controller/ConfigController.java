package com.pharmadesk.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/config")
public class ConfigController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/public")
    public Map<String, Object> getPublicConfiguration() {
        String query = "SELECT config_key, config_value, config_type FROM app_configuration";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(query);
        
        Map<String, Object> configMap = new HashMap<>();
        for (Map<String, Object> row : rows) {
            String key = (String) row.get("config_key");
            String value = (String) row.get("config_value");
            String type = (String) row.get("config_type");
            
            // Basic type casting based on config_type
            if ("integer".equals(type)) {
                configMap.put(key, Integer.parseInt(value));
            } else if ("boolean".equals(type)) {
                configMap.put(key, Boolean.parseBoolean(value));
            } else if ("decimal".equals(type)) {
                configMap.put(key, Double.parseDouble(value));
            } else {
                configMap.put(key, value);
            }
        }
        
        return configMap;
    }
}
