package com.example.travellingapp.controller;

import com.example.travellingapp.dto.request.ForgotPasswordDTO;
import com.example.travellingapp.dto.request.LoginDTO;
import com.example.travellingapp.dto.request.create.CreateUserDTO;
import com.example.travellingapp.response_template.ResponseBody;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/v1/users")
public interface UserController {
    @PostMapping("/register")
    ResponseEntity<ResponseBody<Object>> createNewUser(@Valid @RequestBody CreateUserDTO registerRequest);

    @PostMapping("/register/verify")
    ResponseEntity<ResponseBody<Object>> checkUserDetails(@Valid @RequestBody CreateUserDTO registerRequest);

    @PostMapping("/login")
    ResponseEntity<ResponseBody<Object>> login(@Valid @RequestBody LoginDTO loginRequest);

    @PostMapping("/forgot-password")
    ResponseEntity<ResponseBody<Object>> forgotPassword(@Valid @RequestBody ForgotPasswordDTO forgotPasswordDTO);

    @PostMapping("/logout")
    ResponseEntity<ResponseBody<Object>> logout(@NotNull @RequestHeader(name = "Session-Token") String sessionToken);

    @GetMapping("/check")
    ResponseEntity<ResponseBody<Object>> checkUserExisted(@NotNull @RequestParam(name = "userInput") String userInput);
}