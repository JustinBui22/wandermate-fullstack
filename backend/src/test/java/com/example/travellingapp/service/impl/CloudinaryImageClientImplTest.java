package com.example.travellingapp.service.impl;

import com.cloudinary.Cloudinary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CloudinaryImageClientImplTest {

    @Mock
    private Cloudinary cloudinary;

    private CloudinaryImageClientImpl cloudinaryImageClient;

    @BeforeEach
    void setUp() {
        cloudinaryImageClient = spy(new CloudinaryImageClientImpl(cloudinary));
    }

    @Test
    void deleteOldCloudinaryImageIfChanged_shouldDeleteOldImage_whenPublicIdChanged() {
        doNothing()
                .when(cloudinaryImageClient)
                .deleteImage("old-public-id");

        cloudinaryImageClient.deleteOldCloudinaryImageIfChanged(
                "old-public-id",
                "new-public-id",
                "profile image"
        );

        verify(cloudinaryImageClient).deleteImage("old-public-id");
    }

    @Test
    void deleteOldCloudinaryImageIfChanged_shouldDeleteOldImage_whenNewPublicIdIsNull() {
        doNothing()
                .when(cloudinaryImageClient)
                .deleteImage("old-public-id");

        cloudinaryImageClient.deleteOldCloudinaryImageIfChanged(
                "old-public-id",
                null,
                "trip cover"
        );

        verify(cloudinaryImageClient).deleteImage("old-public-id");
    }

    @Test
    void deleteOldCloudinaryImageIfChanged_shouldNotDeleteImage_whenOldPublicIdIsNull() {
        cloudinaryImageClient.deleteOldCloudinaryImageIfChanged(
                null,
                "new-public-id",
                "profile image"
        );

        verify(cloudinaryImageClient, never()).deleteImage(anyString());
    }

    @Test
    void deleteOldCloudinaryImageIfChanged_shouldNotDeleteImage_whenOldPublicIdIsBlank() {
        cloudinaryImageClient.deleteOldCloudinaryImageIfChanged(
                "   ",
                "new-public-id",
                "profile image"
        );

        verify(cloudinaryImageClient, never()).deleteImage(anyString());
    }

    @Test
    void deleteOldCloudinaryImageIfChanged_shouldNotDeleteImage_whenPublicIdIsUnchanged() {
        cloudinaryImageClient.deleteOldCloudinaryImageIfChanged(
                "same-public-id",
                "same-public-id",
                "profile image"
        );

        verify(cloudinaryImageClient, never()).deleteImage(anyString());
    }

    @Test
    void deleteOldCloudinaryImageIfChanged_shouldNotThrow_whenDeleteImageFails() {
        doThrow(new RuntimeException("Cloudinary delete failed"))
                .when(cloudinaryImageClient)
                .deleteImage("old-public-id");

        assertDoesNotThrow(() ->
                cloudinaryImageClient.deleteOldCloudinaryImageIfChanged(
                        "old-public-id",
                        "new-public-id",
                        "profile image"
                )
        );

        verify(cloudinaryImageClient).deleteImage("old-public-id");
    }
}