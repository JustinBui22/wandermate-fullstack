package com.example.travellingapp.service;

import com.example.travellingapp.dto.request.ForgotPasswordDTO;
import com.example.travellingapp.dto.request.LoginDTO;
import com.example.travellingapp.dto.request.create.CreateUserDTO;
import com.example.travellingapp.response_template.CompleteResponse;

public interface UserService {
    CompleteResponse<Object> createNewUser(CreateUserDTO registerRequest);

    CompleteResponse<Object> checkUserDetails(CreateUserDTO registerRequest);

    CompleteResponse<Object> forgotPassword(ForgotPasswordDTO forgotPasswordDTO);

    CompleteResponse<Object> login(LoginDTO loginRequest);

    CompleteResponse<Object> logout(String sessionToken);

    CompleteResponse<Object> checkUserExisted(String userInput);
}
