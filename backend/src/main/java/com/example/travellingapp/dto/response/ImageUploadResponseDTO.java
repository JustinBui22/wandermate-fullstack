package com.example.travellingapp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ImageUploadResponseDTO {
    private String imageUrl;
    private String publicId;
    private String fileName;
    private String imageType;
}