package com.example.travellingapp.controller;

import com.example.travellingapp.response_template.ResponseBody;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/api/v1/collaboration")
public interface CollaborationSummaryController {

    @GetMapping("/summary")
    ResponseEntity<ResponseBody<Object>> getCollaborationSummary();
}
