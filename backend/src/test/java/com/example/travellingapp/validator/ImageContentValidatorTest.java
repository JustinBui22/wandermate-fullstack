package com.example.travellingapp.validator;

import com.example.travellingapp.exception_handler.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import static com.example.travellingapp.enums.ErrorCodeEnum.INVALID_INPUT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ImageContentValidatorTest {
    private final ImageContentValidator validator = new ImageContentValidator();

    @Test
    void validateAndRead_shouldAcceptPng_whenMimeAndSignatureMatch() throws Exception {
        byte[] png = validRaster("png");

        byte[] result = validator.validateAndRead(file("avatar.png", "image/png", png));

        assertThat(result).isEqualTo(png);
    }

    @Test
    void validateAndRead_shouldRejectTextDisguisedAsPng() {
        MockMultipartFile disguisedFile = file(
                "payload.png",
                "image/png",
                "not an image".getBytes()
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> validator.validateAndRead(disguisedFile)
        );

        assertThat(exception.getErrorCodeEnum()).isEqualTo(INVALID_INPUT);
    }

    @Test
    void validateAndRead_shouldRejectMimeSignatureMismatch() {
        byte[] jpeg = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x01};

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> validator.validateAndRead(file("avatar.png", "image/png", jpeg))
        );

        assertThat(exception.getErrorCodeEnum()).isEqualTo(INVALID_INPUT);
    }

    @Test
    void validateAndRead_shouldRejectTruncatedPngWithValidSignature() {
        byte[] truncatedPng = {
                (byte) 0x89, 0x50, 0x4E, 0x47,
                0x0D, 0x0A, 0x1A, 0x0A,
                0x00
        };

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> validator.validateAndRead(
                        file("truncated.png", "image/png", truncatedPng)
                )
        );

        assertThat(exception.getErrorCodeEnum()).isEqualTo(INVALID_INPUT);
    }

    @Test
    void validateAndRead_shouldAcceptWebpSignature() throws Exception {
        byte[] webp = {
                'R', 'I', 'F', 'F', 0x04, 0x00, 0x00, 0x00,
                'W', 'E', 'B', 'P', 'V', 'P', '8', ' '
        };

        assertThat(validator.validateAndRead(file("cover.webp", "image/webp", webp)))
                .isEqualTo(webp);
    }

    @Test
    void validateAndRead_shouldAcceptHeicCompatibleBrand() throws Exception {
        byte[] heic = {
                0x00, 0x00, 0x00, 0x18,
                'f', 't', 'y', 'p',
                'h', 'e', 'i', 'c',
                0x00
        };

        assertThat(validator.validateAndRead(file("photo.heic", "image/heic", heic)))
                .isEqualTo(heic);
    }

    private MockMultipartFile file(String filename, String contentType, byte[] content) {
        return new MockMultipartFile("file", filename, contentType, content);
    }

    private byte[] validRaster(String format) throws Exception {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        assertThat(ImageIO.write(image, format, output)).isTrue();
        return output.toByteArray();
    }
}
