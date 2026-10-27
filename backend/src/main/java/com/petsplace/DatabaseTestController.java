package com.petsplace;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class DatabaseTestController {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseTestController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/api/db-test")
    public Map<String, Object> testDatabase() {
        String database = jdbcTemplate.queryForObject(
            "SELECT DATABASE()", String.class
        );

        return Map.of(
            "connected", true,
            "database", database,
            "message", "Connected to MySQL successfully"
        );
    }
}