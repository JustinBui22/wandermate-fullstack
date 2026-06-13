package com.example.travellingapp.controller.impl;
import com.example.travellingapp.response_template.CompleteResponse;
import com.example.travellingapp.response_template.ResponseBody;
import com.example.travellingapp.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

import static com.example.travellingapp.enums.CommonEnum.COMMON;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerImplTest {

    private UserService userService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new UserControllerImpl(userService))
                .build();
    }

    @Test
    void checkUserExisted_shouldReturnServiceResponse() throws Exception {
        ResponseBody<Object> responseBody = new ResponseBody<>(
                "E000",
                "Search info successfully",
                COMMON.name(),
                Map.of("exists", false)
        );

        when(userService.checkUserExisted("JustinBo123"))
                .thenReturn(new CompleteResponse<>(responseBody, 200));

        mockMvc.perform(get("/api/v1/users/check")
                        .param("userInput", "JustinBo123")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("E000"))
                .andExpect(jsonPath("$.message").value("Search info successfully"))
                .andExpect(jsonPath("$.flow").value(COMMON.name()))
                .andExpect(jsonPath("$.body.exists").value(false));

        verify(userService).checkUserExisted("JustinBo123");
    }
}