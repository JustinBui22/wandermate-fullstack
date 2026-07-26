package com.example.travellingapp.exception_handler.exception;

import java.io.Serial;

import static com.example.travellingapp.enums.CommonEnum.TOKEN;
import static com.example.travellingapp.enums.ErrorCodeEnum.REFRESH_TOKEN_INVALID;

public class RefreshTokenReuseDetectedException extends BusinessException {
    @Serial
    private static final long serialVersionUID = 1L;

    // This exception is thrown when a refresh token is reused, while still committing:
    // reuseDetected=true, successor-token revocation and session deletion.
    public RefreshTokenReuseDetectedException() {
        super(REFRESH_TOKEN_INVALID, TOKEN.name());
    }
}