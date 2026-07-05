package com.example.travellingapp.service;

import com.example.travellingapp.dto.response.ImageUploadResponseDTO;

import java.io.IOException;

public interface CloudinaryImageClient {
    ImageUploadResponseDTO uploadImage(
            byte[] fileBytes,
            String publicId,
            String cloudinaryFolder,
            String imageType
    ) throws IOException;

    void deleteImage(String publicId) throws IOException;
}
