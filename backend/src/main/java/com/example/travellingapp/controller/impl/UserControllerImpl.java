package com.example.travellingapp.controller.impl;

import com.example.travellingapp.controller.UserController;
import com.example.travellingapp.dto.request.ForgotPasswordDTO;
import com.example.travellingapp.dto.request.LoginDTO;
import com.example.travellingapp.dto.request.create.CreateUserDTO;
import com.example.travellingapp.response_template.CompleteResponse;
import com.example.travellingapp.response_template.ResponseBody;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.example.travellingapp.service.UserService;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserControllerImpl implements UserController {
    private final UserService userService;

    public UserControllerImpl(UserService userService) {
        this.userService = userService;
    }

    public ResponseEntity<ResponseBody<Object>> createNewUser(CreateUserDTO registerRequest) {
        CompleteResponse<Object> response = userService.createNewUser(registerRequest);
        return new ResponseEntity<>(response.getResponseBody(), HttpStatus.valueOf(response.getHttpCode()));
    }

    public ResponseEntity<ResponseBody<Object>> checkUserDetails(CreateUserDTO registerRequest) {
        CompleteResponse<Object> response = userService.checkUserDetails(registerRequest);
        return new ResponseEntity<>(response.getResponseBody(), HttpStatus.valueOf(response.getHttpCode()));
    }

    public ResponseEntity<ResponseBody<Object>> forgotPassword(ForgotPasswordDTO forgotPasswordDTO) {
        CompleteResponse<Object> response = userService.forgotPassword(forgotPasswordDTO);
        return new ResponseEntity<>(response.getResponseBody(), HttpStatus.valueOf(response.getHttpCode()));
    }

    public ResponseEntity<ResponseBody<Object>> login(LoginDTO loginRequest) {
        CompleteResponse<Object> response = userService.login(loginRequest);
        return new ResponseEntity<>(response.getResponseBody(), HttpStatus.valueOf(response.getHttpCode()));
    }

    public ResponseEntity<ResponseBody<Object>> logout(String sessionToken) {
        CompleteResponse<Object> response = userService.logout(sessionToken);
        return new ResponseEntity<>(response.getResponseBody(), HttpStatus.valueOf(response.getHttpCode()));
    }

    @Override
    public ResponseEntity<ResponseBody<Object>> checkUserExisted(String userInput) {
        CompleteResponse<Object> response = userService.checkUserExisted(userInput);
        return new ResponseEntity<>(response.getResponseBody(), HttpStatus.valueOf(response.getHttpCode()));
    }
}


