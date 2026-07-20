package com.example.travellingapp.validator;

import com.example.travellingapp.exception_handler.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Locale;
import java.util.regex.Pattern;

import static com.example.travellingapp.enums.CommonEnum.COMMON;
import static com.example.travellingapp.enums.ErrorCodeEnum.INVALID_INPUT;

@Component
public class ImageReferenceValidator {
    private static final Pattern UUID_PATTERN = Pattern.compile(
            "[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}"
    );
    private static final Pattern IMAGE_EXTENSION_PATTERN = Pattern.compile(
            "\\.(png|jpe?g|webp|heic|heif)$",
            Pattern.CASE_INSENSITIVE
    );

    private final String cloudinaryBaseFolder;

    public ImageReferenceValidator(
            @Value("${cloudinary.base-folder:wandermate}") String cloudinaryBaseFolder
    ) {
        this.cloudinaryBaseFolder = normalizeBaseFolder(cloudinaryBaseFolder);
    }

    public void validateProfileImageReference(String imageUrl, String publicId, Long userId) {
        validateImageReference(
                imageUrl,
                publicId,
                userId,
                "profile-images",
                "profile"
        );
    }

    public void validateTripCoverReference(String imageUrl, String publicId, Long userId) {
        validateImageReference(
                imageUrl,
                publicId,
                userId,
                "trip-covers",
                "trip-cover"
        );
    }

    private void validateImageReference(
            String imageUrl,
            String publicId,
            Long userId,
            String uploadFolder,
            String publicIdPrefix
    ) {
        String normalizedImageUrl = trimToNull(imageUrl);
        String normalizedPublicId = trimToNull(publicId);

        if (normalizedImageUrl == null && normalizedPublicId == null) {
            return;
        }

        if (normalizedImageUrl == null || normalizedPublicId == null || userId == null) {
            throw invalidInput();
        }

        String expectedPublicIdPrefix = cloudinaryBaseFolder
                + "/"
                + uploadFolder
                + "/users/"
                + userId
                + "/"
                + publicIdPrefix
                + "-"
                + userId
                + "-";

        if (!normalizedPublicId.startsWith(expectedPublicIdPrefix)) {
            throw invalidInput();
        }

        String uuid = normalizedPublicId.substring(expectedPublicIdPrefix.length());
        if (!UUID_PATTERN.matcher(uuid).matches()) {
            throw invalidInput();
        }

        validateCloudinaryUrl(normalizedImageUrl, normalizedPublicId);
    }

    private void validateCloudinaryUrl(String imageUrl, String publicId) {
        try {
            URI uri = URI.create(imageUrl);
            String scheme = uri.getScheme();
            String host = uri.getHost();
            String path = uri.getPath();

            if (!"https".equalsIgnoreCase(scheme)
                    || host == null
                    || !"res.cloudinary.com".equalsIgnoreCase(host)
                    || path == null) {
                throw invalidInput();
            }

            String uploadMarker = "/image/upload/";
            int uploadMarkerIndex = path.indexOf(uploadMarker);
            if (uploadMarkerIndex < 0) {
                throw invalidInput();
            }

            String uploadPath = path.substring(uploadMarkerIndex + uploadMarker.length());
            int publicIdIndex = uploadPath.indexOf(publicId);
            if (publicIdIndex < 0
                    || (publicIdIndex > 0 && uploadPath.charAt(publicIdIndex - 1) != '/')) {
                throw invalidInput();
            }

            String suffix = uploadPath.substring(publicIdIndex + publicId.length());
            if (!suffix.isEmpty() && !IMAGE_EXTENSION_PATTERN.matcher(suffix).matches()) {
                throw invalidInput();
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw invalidInput();
        }
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

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmedValue = value.trim();
        return trimmedValue.isEmpty() ? null : trimmedValue;
    }

    private BusinessException invalidInput() {
        return new BusinessException(INVALID_INPUT, COMMON.name());
    }
}