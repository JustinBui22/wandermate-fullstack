package com.example.travellingapp.controller.impl;

import com.example.travellingapp.response_template.CompleteResponse;
import com.example.travellingapp.response_template.ResponseBody;
import com.example.travellingapp.service.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

import static com.example.travellingapp.enums.CommonEnum.TOKEN;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TokenControllerImplTest {

    private TokenService tokenService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        tokenService = mock(TokenService.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new TokenControllerImpl(tokenService))
                .build();
    }

    @Test
    void refreshAccessToken_shouldReturnServiceResponse() throws Exception {
        ResponseBody<Object> responseBody = new ResponseBody<>(
                "E000",
                "Token generated successfully",
                TOKEN.name(),
                Map.of("accessToken", "new-access-token")
        );

        when(tokenService.refreshAccessToken("refresh-token", "session-token"))
                .thenReturn(new CompleteResponse<>(responseBody, 200));

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .header("Refresh-Token", "refresh-token")
                        .header("Session-Token", "session-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("E000"))
                .andExpect(jsonPath("$.flow").value(TOKEN.name()))
                .andExpect(jsonPath("$.body.accessToken").value("new-access-token"));

        verify(tokenService).refreshAccessToken("refresh-token", "session-token");
    }
}
