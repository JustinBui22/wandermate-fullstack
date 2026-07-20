package com.example.travellingapp.validator;

import com.example.travellingapp.exception_handler.exception.BusinessException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import static com.example.travellingapp.enums.CommonEnum.COMMON;
import static com.example.travellingapp.enums.ErrorCodeEnum.INVALID_INPUT;

@Component
public class ImageContentValidator {
    private static final long MAX_IMAGE_SIZE_BYTES = 5L * 1024L * 1024L;
    private static final long MAX_DECODED_PIXELS = 20_000_000L;
    private static final Map<String, String> MIME_TO_FORMAT = Map.of(
            "image/jpeg", "jpeg",
            "image/jpg", "jpeg",
            "image/png", "png",
            "image/webp", "webp",
            "image/heic", "heif",
            "image/heif", "heif"
    );
    private static final Set<String> HEIF_BRANDS = Set.of(
            "heic", "heix", "hevc", "hevx", "heim", "heis", "hevm", "hevs",
            "mif1", "msf1"
    );

    public byte[] validateAndRead(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw invalidInput();
        }

        if (file.getSize() > MAX_IMAGE_SIZE_BYTES) {
            throw invalidInput();
        }

        String declaredContentType = normalizeContentType(file.getContentType());
        String declaredFormat = MIME_TO_FORMAT.get(declaredContentType);
        if (declaredFormat == null) {
            throw invalidInput();
        }

        byte[] bytes = file.getBytes();
        if (bytes.length == 0 || bytes.length > MAX_IMAGE_SIZE_BYTES) {
            throw invalidInput();
        }

        String detectedFormat = detectFormat(bytes);
        if (!declaredFormat.equals(detectedFormat)) {
            throw invalidInput();
        }

        // The JDK can decode PNG/JPEG without extra codecs. Decode those
        // formats so a matching magic header alone is not considered valid.
        // WebP/HEIF still receive strict container-signature checks and are
        // decoded again by Cloudinary, which supports those formats.
        if ("png".equals(detectedFormat) || "jpeg".equals(detectedFormat)) {
            verifyDecodableRasterImage(bytes);
        }

        return bytes;
    }

    private void verifyDecodableRasterImage(byte[] bytes) {
        try (ImageInputStream input = ImageIO.createImageInputStream(
                new ByteArrayInputStream(bytes)
        )) {
            if (input == null) {
                throw invalidInput();
            }

            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) {
                throw invalidInput();
            }

            ImageReader reader = readers.next();
            try {
                reader.setInput(input, true, true);
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                long pixels = (long) width * height;

                if (width <= 0 || height <= 0 || pixels > MAX_DECODED_PIXELS) {
                    throw invalidInput();
                }

                if (reader.read(0) == null) {
                    throw invalidInput();
                }
            } finally {
                reader.dispose();
            }
        } catch (BusinessException e) {
            throw e;
        } catch (IOException | RuntimeException e) {
            throw invalidInput();
        }
    }

    private String normalizeContentType(String contentType) {
        if (contentType == null) {
            return "";
        }

        int parameterIndex = contentType.indexOf(';');
        String baseType = parameterIndex >= 0
                ? contentType.substring(0, parameterIndex)
                : contentType;
        return baseType.trim().toLowerCase(Locale.ROOT);
    }

    private String detectFormat(byte[] bytes) {
        if (hasPrefix(bytes, 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)) {
            return "png";
        }

        if (hasPrefix(bytes, 0xFF, 0xD8, 0xFF)) {
            return "jpeg";
        }

        if (matchesAscii(bytes, 0, "RIFF") && matchesAscii(bytes, 8, "WEBP")) {
            return "webp";
        }

        if (matchesAscii(bytes, 4, "ftyp") && bytes.length >= 12) {
            String brand = new String(bytes, 8, 4, StandardCharsets.US_ASCII)
                    .toLowerCase(Locale.ROOT);
            if (HEIF_BRANDS.contains(brand)) {
                return "heif";
            }
        }

        return "unknown";
    }

    private boolean hasPrefix(byte[] bytes, int... expected) {
        if (bytes.length < expected.length) {
            return false;
        }

        for (int i = 0; i < expected.length; i++) {
            if ((bytes[i] & 0xFF) != expected[i]) {
                return false;
            }
        }
        return true;
    }

    private boolean matchesAscii(byte[] bytes, int offset, String expected) {
        byte[] expectedBytes = expected.getBytes(StandardCharsets.US_ASCII);
        if (bytes.length < offset + expectedBytes.length) {
            return false;
        }

        for (int i = 0; i < expectedBytes.length; i++) {
            if (bytes[offset + i] != expectedBytes[i]) {
                return false;
            }
        }
        return true;
    }

    private BusinessException invalidInput() {
        return new BusinessException(INVALID_INPUT, COMMON.name());
    }
}
