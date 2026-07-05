package com.example.travellingapp.service.impl;

import com.example.travellingapp.dto.response.ImageUploadResponseDTO;
import com.example.travellingapp.entity.User;
import com.example.travellingapp.exception_handler.exception.BusinessException;
import com.example.travellingapp.repository.ErrorCodeRepository;
import com.example.travellingapp.repository.UserRepository;
import com.example.travellingapp.response_template.CompleteResponse;
import com.example.travellingapp.security.data_security.AuthenticatedUserProvider;
import com.example.travellingapp.service.CloudinaryImageClient;
import com.example.travellingapp.service.ImageUploadService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import static com.example.travellingapp.enums.CommonEnum.COMMON;
import static com.example.travellingapp.enums.ErrorCodeEnum.*;
import static com.example.travellingapp.response_template.CompleteResponse.getCompleteResponse;

@Service
@Log4j2
public class ImageUploadServiceImpl implements ImageUploadService {
    private static final long MAX_IMAGE_SIZE_BYTES = 5L * 1024L * 1024L;
    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/webp",
            "image/heic",
            "image/heif"
    );
    private static final Set<String> ALLOWED_UPLOAD_FOLDERS = Set.of(
            "profile-images",
            "trip-covers"
    );

    private final ErrorCodeRepository errorCodeRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final CloudinaryImageClient cloudinaryImageClient;
    private final String cloudinaryBaseFolder;
    private final UserRepository userRepository;

    public ImageUploadServiceImpl(
            ErrorCodeRepository errorCodeRepository,
            AuthenticatedUserProvider authenticatedUserProvider,
            CloudinaryImageClient cloudinaryImageClient,
            @Value("${cloudinary.base-folder:wandermate}") String cloudinaryBaseFolder,
            UserRepository userRepository
    ) {
        this.errorCodeRepository = errorCodeRepository;
        this.authenticatedUserProvider = authenticatedUserProvider;
        this.cloudinaryImageClient = cloudinaryImageClient;
        this.cloudinaryBaseFolder = normalizeBaseFolder(cloudinaryBaseFolder);
        this.userRepository = userRepository;
    }

    @Override
    public CompleteResponse<Object> uploadImage(MultipartFile file, String imageType) {
        try {
            String username = authenticatedUserProvider.getUsername();
            User currentUser = userRepository.findByUsernameAndActive(username)
                    .orElseThrow(() -> new BusinessException(USER_NOT_FOUND, COMMON.name()));

            String uploadFolder = normalizeUploadFolder(imageType);
            validateImageFile(file);

            String cloudinaryFolder = cloudinaryBaseFolder
                    + "/"
                    + uploadFolder
                    + "/users/"
                    + currentUser.getUserId();

            String publicId = buildPublicId(uploadFolder, currentUser.getUserId());
            log.info("Uploading image for user: {}, publicId: {}, cloudinaryFolder: {}", username, publicId, cloudinaryFolder);
            ImageUploadResponseDTO responseDTO = cloudinaryImageClient.uploadImage(
                    file.getBytes(),
                    publicId,
                    cloudinaryFolder,
                    uploadFolder
            );

            return getCompleteResponse(
                    errorCodeRepository,
                    SEARCH_INFO_SUCCESS,
                    COMMON.name(),
                    responseDTO
            );
        } catch (BusinessException e) {
            throw e;
        } catch (IOException e) {
            log.error("Failed to upload image to Cloudinary.", e);
            throw new BusinessException(INTERNAL_SERVER_ERROR, COMMON.name());
        } catch (Exception e) {
            log.error("Unexpected image upload failure.", e);
            throw new BusinessException(INTERNAL_SERVER_ERROR, COMMON.name());
        }
    }

    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(INVALID_INPUT, COMMON.name());
        }

        if (file.getSize() > MAX_IMAGE_SIZE_BYTES) {
            throw new BusinessException(INVALID_INPUT, COMMON.name());
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new BusinessException(INVALID_INPUT, COMMON.name());
        }
    }

    private String normalizeUploadFolder(String imageType) {
        String normalizedType = imageType == null
                ? ""
                : imageType.trim().toLowerCase(Locale.ROOT);

        if (!ALLOWED_UPLOAD_FOLDERS.contains(normalizedType)) {
            throw new BusinessException(INVALID_INPUT, COMMON.name());
        }

        return normalizedType;
    }

    private String normalizeBaseFolder(String value) {
        String normalizedValue = value == null
                ? "wandermate"
                : value.trim().toLowerCase(Locale.ROOT);

        normalizedValue = normalizedValue
                .replace("\\", "/")
                .replaceAll("^/+", "")
                .replaceAll("/+$", "");

        return normalizedValue.isBlank() ? "wandermate" : normalizedValue;
    }

    private String buildPublicId(String username) {
        String safeUsername = username == null
                ? "user"
                : username.trim().toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9_-]", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");

        if (safeUsername.isBlank()) {
            safeUsername = "user";
        }

        return safeUsername + "-" + UUID.randomUUID();
    }

    private String buildPublicId(String uploadFolder, Long userId) {
        String prefix = "profile-images".equals(uploadFolder) ? "profile" : "trip-cover";
        return prefix + "-" + userId + "-" + UUID.randomUUID();
    }
}
