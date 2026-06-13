package com.example.travellingapp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequestMapping("/api/v1/health")
public interface HealthController {

    @GetMapping
    ResponseEntity<Map<String, String>> health();
}