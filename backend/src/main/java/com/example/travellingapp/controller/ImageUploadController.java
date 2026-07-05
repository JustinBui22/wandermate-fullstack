package com.example.travellingapp.controller;

import com.example.travellingapp.response_template.ResponseBody;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@RequestMapping("/api/v1/uploads")
public interface ImageUploadController {
    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<ResponseBody<Object>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("imageType") String imageType
    );
}
