package com.example.travellingapp.controller.impl;

import com.example.travellingapp.controller.HealthController;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import java.util.Map;


@Controller
public class HealthControllerImpl implements HealthController {

    @Override
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "WanderMate backend"
        ));
    }
}
