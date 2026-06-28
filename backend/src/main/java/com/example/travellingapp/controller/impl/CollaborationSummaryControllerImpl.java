package com.example.travellingapp.controller.impl;

import com.example.travellingapp.controller.CollaborationSummaryController;
import com.example.travellingapp.response_template.CompleteResponse;
import com.example.travellingapp.response_template.ResponseBody;
import com.example.travellingapp.service.CollaborationSummaryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CollaborationSummaryControllerImpl implements CollaborationSummaryController {

    private final CollaborationSummaryService collaborationSummaryService;

    public CollaborationSummaryControllerImpl(CollaborationSummaryService collaborationSummaryService) {
        this.collaborationSummaryService = collaborationSummaryService;
    }

    @Override
    public ResponseEntity<ResponseBody<Object>> getCollaborationSummary() {
        CompleteResponse<Object> response = collaborationSummaryService.getCollaborationSummary();
        return new ResponseEntity<>(response.getResponseBody(), HttpStatus.valueOf(response.getHttpCode()));
    }
}