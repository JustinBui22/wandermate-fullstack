package com.example.travellingapp.controller.impl;

import com.example.travellingapp.controller.TokenController;
import com.example.travellingapp.service.TokenService;
import com.example.travellingapp.response_template.CompleteResponse;
import com.example.travellingapp.response_template.ResponseBody;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TokenControllerImpl implements TokenController {
    private final TokenService tokenService;

    public TokenControllerImpl(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public ResponseEntity<ResponseBody<Object>> refreshAccessToken(String refreshToken, String sessionToken) {
        CompleteResponse<Object> response = tokenService.refreshAccessToken(refreshToken, sessionToken);
        return new ResponseEntity<>(response.getResponseBody(), HttpStatus.valueOf(response.getHttpCode()));
    }
}
