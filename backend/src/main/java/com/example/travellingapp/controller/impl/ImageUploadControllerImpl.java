package com.example.travellingapp.controller.impl;

import com.example.travellingapp.controller.ImageUploadController;
import com.example.travellingapp.response_template.CompleteResponse;
import com.example.travellingapp.response_template.ResponseBody;
import com.example.travellingapp.service.ImageUploadService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class ImageUploadControllerImpl implements ImageUploadController {
    private final ImageUploadService imageUploadService;

    public ImageUploadControllerImpl(ImageUploadService imageUploadService) {
        this.imageUploadService = imageUploadService;
    }

    @Override
    public ResponseEntity<ResponseBody<Object>> uploadImage(MultipartFile file, String imageType) {
        CompleteResponse<Object> response = imageUploadService.uploadImage(file, imageType);
        return new ResponseEntity<>(response.getResponseBody(), HttpStatus.valueOf(response.getHttpCode()));
    }
}
