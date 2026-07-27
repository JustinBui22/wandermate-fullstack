package com.example.travellingapp.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.travellingapp.dto.response.ImageUploadResponseDTO;
import com.example.travellingapp.exception_handler.exception.BusinessException;
import com.example.travellingapp.service.CloudinaryImageClient;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

import static com.example.travellingapp.enums.CommonEnum.COMMON;
import static com.example.travellingapp.enums.ErrorCodeEnum.DELETE_IMAGE_FAIL;

@Log4j2
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

        log.info(
                "Image uploaded successfully."
        );

        String finalPublicId = returnedPublicId == null || returnedPublicId.isBlank()
                ? publicId
                : returnedPublicId;

        return new ImageUploadResponseDTO(
                secureUrl,
                finalPublicId,
                finalPublicId,
                imageType
        );
    }

    @Override
    public void deleteImage(String publicId) {
        try {
            if (publicId == null || publicId.isBlank()) {
                return;
            }
            cloudinary.uploader().destroy(
                    publicId,
                    ObjectUtils.asMap("resource_type", "image")
            );
        } catch (IOException e) {
            log.error(
                    "Failed to delete Cloudinary image: {}",
                    e.getClass().getSimpleName()
            );
            throw new BusinessException(DELETE_IMAGE_FAIL, COMMON.name());
        }
    }

    @Override
    public void deleteOldCloudinaryImageIfChanged(String oldPublicId, String newPublicId, String imagePurpose) {
        if (oldPublicId == null || oldPublicId.isBlank()) {
            return;
        }

        // Do not delete if the public ID did not actually change.
        if (oldPublicId.equals(newPublicId)) {
            return;
        }

        try {
            deleteImage(oldPublicId);
        } catch (Exception e) {
            log.error("Failed to delete old Cloudinary {}: {}", imagePurpose, e.getClass().getSimpleName()
            );
        }
    }

    private String toStringOrNull(Object value) {
        return value == null ? null : value.toString();
    }
}