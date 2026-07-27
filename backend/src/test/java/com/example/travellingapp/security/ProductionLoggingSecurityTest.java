package com.example.travellingapp.security;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ProductionLoggingSecurityTest {

    private static final Pattern LOG_CALL_PATTERN = Pattern.compile(
            "log\\.(?:trace|debug|info|warn|error)\\s*\\((?s:.*?)\\);"
    );

    private static final List<String> FORBIDDEN_LOG_FRAGMENTS = List.of(
            "getOtp()",
            "getPassword()",
            "getNewPassword()",
            "getEmail()",
            "getPhoneNumber()",
            "authHeader",
            "accessToken",
            "refreshToken",
            "sessionToken",
            "sessionId",
            "shareCode.getCode()",
            "normalizedCode",
            "secureUrl",
            "publicId",
            "cloudinaryFolder",
            "currentTrip,",
            "otpDTO",
            "registerRequest",
            "forgotPasswordDTO",
            "loginRequest",
            "updateUserProfileDTO",
            "updateUserSettingsDTO"
    );

    @Test
    void productionLogStatements_shouldNotReferenceSensitiveValuesOrRequestDtos() throws IOException {
        Path sourceRoot = Path.of("src", "main", "java");
        List<String> violations = new ArrayList<>();

        try (Stream<Path> sourceFiles = Files.walk(sourceRoot)) {
            sourceFiles
                    .filter(path -> path.toString().endsWith(".java"))
                    .forEach(path -> inspectLogStatements(path, violations));
        }

        assertThat(violations)
                .as("Sensitive values and request DTOs must not be written to application logs")
                .isEmpty();
    }

    @Test
    void productionProfile_shouldDisableRequestDetailsAndSqlBindingLogs() throws IOException {
        String properties = Files.readString(
                Path.of("src", "main", "resources", "application-prod.properties")
        );

        assertThat(properties)
                .contains("spring.mvc.log-request-details=false")
                .contains("logging.level.org.hibernate.SQL=OFF")
                .contains("logging.level.org.hibernate.orm.jdbc.bind=OFF")
                .contains("logging.level.org.apache.hc.client5.http.headers=OFF")
                .contains("logging.level.org.apache.hc.client5.http.wire=OFF");
    }

    private void inspectLogStatements(Path sourceFile, List<String> violations) {
        try {
            String source = Files.readString(sourceFile);
            Matcher matcher = LOG_CALL_PATTERN.matcher(source);

            while (matcher.find()) {
                String logStatement = matcher.group();

                for (String forbiddenFragment : FORBIDDEN_LOG_FRAGMENTS) {
                    if (logStatement.contains(forbiddenFragment)) {
                        violations.add(
                                sourceFile + " contains sensitive log fragment: " + forbiddenFragment
                        );
                    }
                }
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to inspect " + sourceFile, exception);
        }
    }
}
