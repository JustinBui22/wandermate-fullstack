package com.example.travellingapp.validator;

import com.example.travellingapp.exception_handler.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.example.travellingapp.enums.CommonEnum.COMMON;
import static com.example.travellingapp.enums.ErrorCodeEnum.INVALID_INPUT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ImageReferenceValidatorTest {
    private static final Long USER_ID = 1L;
    private static final String UUID = "123e4567-e89b-12d3-a456-426614174000";

    private ImageReferenceValidator imageReferenceValidator;

    @BeforeEach
    void setUp() {
        imageReferenceValidator = new ImageReferenceValidator("wandermate");
    }

    @Test
    void validateProfileImageReference_shouldAcceptOwnedCloudinaryReference() {
        String publicId = "wandermate/profile-images/users/1/profile-1-" + UUID;
        String imageUrl = "https://res.cloudinary.com/demo/image/upload/v123/" + publicId + ".png";

        assertThatCode(() -> imageReferenceValidator.validateProfileImageReference(
                imageUrl,
                publicId,
                USER_ID
        )).doesNotThrowAnyException();
    }

    @Test
    void validateTripCoverReference_shouldAcceptOwnedCloudinaryReferenceWithTransformation() {
        String publicId = "wandermate/trip-covers/users/1/trip-cover-1-" + UUID;
        String imageUrl = "https://res.cloudinary.com/demo/image/upload/c_fill,w_1200/v123/"
                + publicId
                + ".jpg";

        assertThatCode(() -> imageReferenceValidator.validateTripCoverReference(
                imageUrl,
                publicId,
                USER_ID
        )).doesNotThrowAnyException();
    }

    @Test
    void validateProfileImageReference_shouldAcceptBlankReferenceForRemoval() {
        assertThatCode(() -> imageReferenceValidator.validateProfileImageReference(
                " ",
                " ",
                USER_ID
        )).doesNotThrowAnyException();
    }

    @Test
    void validateProfileImageReference_shouldRejectMissingImageUrl() {
        String publicId = "wandermate/profile-images/users/1/profile-1-" + UUID;

        assertInvalidInput(() -> imageReferenceValidator.validateProfileImageReference(
                null,
                publicId,
                USER_ID
        ));
    }

    @Test
    void validateProfileImageReference_shouldRejectMissingPublicId() {
        String imageUrl = "https://res.cloudinary.com/demo/image/upload/v123/"
                + "wandermate/profile-images/users/1/profile-1-"
                + UUID
                + ".png";

        assertInvalidInput(() -> imageReferenceValidator.validateProfileImageReference(
                imageUrl,
                null,
                USER_ID
        ));
    }

    @Test
    void validateProfileImageReference_shouldRejectDifferentUserFolder() {
        String publicId = "wandermate/profile-images/users/2/profile-2-" + UUID;
        String imageUrl = "https://res.cloudinary.com/demo/image/upload/v123/" + publicId + ".png";

        assertInvalidInput(() -> imageReferenceValidator.validateProfileImageReference(
                imageUrl,
                publicId,
                USER_ID
        ));
    }

    @Test
    void validateTripCoverReference_shouldRejectWrongGeneratedPublicIdPrefix() {
        String publicId = "wandermate/trip-covers/users/1/profile-1-" + UUID;
        String imageUrl = "https://res.cloudinary.com/demo/image/upload/v123/" + publicId + ".jpg";

        assertInvalidInput(() -> imageReferenceValidator.validateTripCoverReference(
                imageUrl,
                publicId,
                USER_ID
        ));
    }

    @Test
    void validateProfileImageReference_shouldRejectNonCloudinaryHost() {
        String publicId = "wandermate/profile-images/users/1/profile-1-" + UUID;
        String imageUrl = "https://example.com/image/upload/v123/" + publicId + ".png";

        assertInvalidInput(() -> imageReferenceValidator.validateProfileImageReference(
                imageUrl,
                publicId,
                USER_ID
        ));
    }

    @Test
    void validateProfileImageReference_shouldRejectInsecureCloudinaryUrl() {
        String publicId = "wandermate/profile-images/users/1/profile-1-" + UUID;
        String imageUrl = "http://res.cloudinary.com/demo/image/upload/v123/" + publicId + ".png";

        assertInvalidInput(() -> imageReferenceValidator.validateProfileImageReference(
                imageUrl,
                publicId,
                USER_ID
        ));
    }

    @Test
    void validateProfileImageReference_shouldRejectMismatchedUrlAndPublicId() {
        String publicId = "wandermate/profile-images/users/1/profile-1-" + UUID;
        String otherPublicId = "wandermate/profile-images/users/1/profile-1-"
                + "123e4567-e89b-12d3-a456-426614174001";
        String imageUrl = "https://res.cloudinary.com/demo/image/upload/v123/"
                + otherPublicId
                + ".png";

        assertInvalidInput(() -> imageReferenceValidator.validateProfileImageReference(
                imageUrl,
                publicId,
                USER_ID
        ));
    }

    private void assertInvalidInput(Runnable runnable) {
        BusinessException exception = assertThrows(BusinessException.class, runnable::run);

        assertThat(exception.getErrorCodeEnum()).isEqualTo(INVALID_INPUT);
        assertThat(exception.getFlow()).isEqualTo(COMMON.name());
    }
}
