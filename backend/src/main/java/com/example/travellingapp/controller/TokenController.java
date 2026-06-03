package com.example.travellingapp.controller;

import com.example.travellingapp.response_template.ResponseBody;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RequestMapping("/api/v1/auth")
public interface TokenController {
    @PostMapping("/refresh")
    ResponseEntity<ResponseBody<Object>> refreshAccessToken(@NotNull @RequestHeader(value = "Refresh-Token") String refreshToken,
                                                            @NotNull @RequestHeader(value = "Session-Token") String sessionToken);
}
