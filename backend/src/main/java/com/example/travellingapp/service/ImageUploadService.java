package com.example.travellingapp.service;

import com.example.travellingapp.response_template.CompleteResponse;
import org.springframework.web.multipart.MultipartFile;

public interface ImageUploadService {
    CompleteResponse<Object> uploadImage(MultipartFile file, String imageType);
}
