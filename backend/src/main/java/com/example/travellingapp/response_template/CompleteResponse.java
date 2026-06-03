package com.example.travellingapp.response_template;

import com.example.travellingapp.entity.ErrorCodeEntity;
import com.example.travellingapp.enums.ErrorCodeEnum;
import com.example.travellingapp.enums.HttpStatusCodeEnum;
import com.example.travellingapp.repository.ErrorCodeRepository;


import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.util.Optional;

import static com.example.travellingapp.enums.ErrorCodeEnum.CONFIG_NOT_FOUND;
import static com.example.travellingapp.util.Common.*;

@Log4j2
@Setter
@Getter
public final class CompleteResponse<T> {
    private ResponseBody<T> responseBody;
    private int httpCode;

    public CompleteResponse(ResponseBody<T> responseBody, int httpCode) {
        this.responseBody = responseBody;
        this.httpCode = httpCode;
    }

    public static CompleteResponse<Object> getCompleteResponse(ErrorCodeRepository errorCodeRepository, ErrorCodeEnum errorCodeEnum, String flow, Object object) {
        Optional<ErrorCodeEntity> errorCodeOptional = errorCodeRepository.findByErrorEnumAndFlow(errorCodeEnum.name(), flow);
        if (errorCodeOptional.isEmpty()) {
            log.info("There is no error code {} in flow {}, removing flow.", errorCodeEnum.name(), flow);
            errorCodeOptional = errorCodeRepository.findFirstByErrorEnum(errorCodeEnum.name());
            if (errorCodeOptional.isEmpty()) {
                log.info("There is no error code {}", errorCodeEnum.name());
                return new CompleteResponse<>(new ResponseBody<>(CONFIG_NOT_FOUND.getCode(), CONFIG_NOT_FOUND.getMessage(), CONFIG_NOT_FOUND.getFlow().name(), null), CONFIG_NOT_FOUND.getHttpStatusCodeEnum().value);
            }
        }
        String errorCode = getErrorCode(errorCodeOptional.get());
        String errorMessage = getErrorCodeMessage(errorCodeOptional.get());
        String errorDescription = errorCodeOptional.map(ErrorCodeEntity::getErrorDescription).orElse(null);
        HttpStatusCodeEnum httpStatusCode = getHttpFromErrorCode(errorCodeOptional.get().getErrorEnum());
        if (object == null) {
            return new CompleteResponse<>(new ResponseBody<>(errorCode, errorMessage, flow, errorDescription), httpStatusCode.value);
        }
        return new CompleteResponse<>(new ResponseBody<>(errorCode, errorMessage, flow, object), httpStatusCode.value());
    }

}



