package com.example.travellingapp.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.travellingapp.dto.response.ImageUploadResponseDTO;
import com.example.travellingapp.service.CloudinaryImageClient;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryImageClientImpl implements CloudinaryImageClient {
    private final Cloudinary cloudinary;

    public CloudinaryImageClientImpl(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    @Override
    public ImageUploadResponseDTO uploadImage(
            byte[] fileBytes,
            String publicId,
            String cloudinaryFolder,
            String imageType
    ) throws IOException {
        Map<?, ?> uploadResult = cloudinary.uploader().upload(
                fileBytes,
                ObjectUtils.asMap(
                        "folder", cloudinaryFolder,
                        "public_id", publicId,
                        "resource_type", "image",
                        "use_filename", false,
                        "unique_filename", false,
                        "overwrite", false
                )
        );

        String secureUrl = toStringOrNull(uploadResult.get("secure_url"));
        String returnedPublicId = toStringOrNull(uploadResult.get("public_id"));

        if (secureUrl == null || secureUrl.isBlank()) {
            throw new IOException("Cloudinary upload did not return a secure URL.");
        }

        return new ImageUploadResponseDTO(
                secureUrl,
                returnedPublicId == null || returnedPublicId.isBlank() ? publicId : returnedPublicId,
                imageType
        );
    }

    private String toStringOrNull(Object value) {
        return value == null ? null : value.toString();
    }
}
