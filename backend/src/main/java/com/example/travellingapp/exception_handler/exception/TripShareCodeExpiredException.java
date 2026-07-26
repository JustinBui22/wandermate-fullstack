package com.example.travellingapp.exception_handler.exception;

import java.io.Serial;

import static com.example.travellingapp.enums.CommonEnum.TRIP_MEMBER;
import static com.example.travellingapp.enums.ErrorCodeEnum.TRIP_SHARE_CODE_EXPIRED;

public class TripShareCodeExpiredException extends BusinessException {
    @Serial
    private static final long serialVersionUID = 1L;

    public TripShareCodeExpiredException() {
        super(TRIP_SHARE_CODE_EXPIRED, TRIP_MEMBER.name());
    }
}
