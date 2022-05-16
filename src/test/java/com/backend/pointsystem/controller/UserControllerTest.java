package com.backend.pointsystem.controller;

import com.backend.pointsystem.dto.request.CreateUserRequest;
import com.backend.pointsystem.dto.request.LoginRequest;
import com.backend.pointsystem.exception.DuplicateUserException;
import com.backend.pointsystem.exception.UserNotFoundException;
import com.backend.pointsystem.security.WebSecurityConfig;
import com.backend.pointsystem.security.jwt.JwtAuthenticationFilter;
import com.backend.pointsystem.security.jwt.JwtProvider;
import com.backend.pointsystem.security.jwt.Token;
import com.backend.pointsystem.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;

import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(controllers = UserController.class,
        excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = WebSecurityConfig.class)} )
@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
@MockBean(JpaMetamodelMappingContext.class)
class UserControllerTest {

    @MockBean
    private UserService userService;

    @MockBean
    private JwtAuthenticationFilter filter;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;


    @BeforeEach
    void setUp(WebApplicationContext context, RestDocumentationContextProvider restDocumentation) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(documentationConfiguration(restDocumentation))
                .build();
    }


    @Test
    @DisplayName("유저 회원가입 - 성공")
    void accountUser() throws Exception {
        //given
        given(userService.signUp(any(CreateUserRequest.class))).willReturn(1L);

        //when
        ResultActions result = mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8)
                .content(objectMapper.writeValueAsString(new CreateUserRequest("도훈", "ehgns852", "123123", 100000))));

        //then
        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(1L))
                .andDo(document("user/create",
                        requestFields(
                                fieldWithPath("name").description("회원 이름"),
                                fieldWithPath("username").description("회원 ID"),
                                fieldWithPath("password").description("비밀번호"),
                                fieldWithPath("asset").description("보유 자산")
                        ),
                        responseFields(
                                fieldWithPath("userId").description("회원 PK")
                        )));

        verify(userService, times(1)).signUp(any(CreateUserRequest.class));
    }

    @Test
    @DisplayName("유저 회원가입 - 회원 ID가 중복된 경우 실패")
    void accountUserFail() throws Exception {
        //given

        doThrow(new DuplicateUserException("이미 존재하는 ID 입니다."))
                .when(userService).signUp(any(CreateUserRequest.class));

        //when
        ResultActions result = mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8)
                .content(objectMapper.writeValueAsString(new CreateUserRequest("도훈", "ehgns852", "123123", 100000))));

        //then
        result.andExpect(status().isBadRequest())
                .andDo(document("user/create/fail"));
        
        verify(userService, times(1)).signUp(any(CreateUserRequest.class));
    }

    @Test
    @DisplayName("회원 로그인 - 성공")
    void login() throws Exception {
        //given
        LoginRequest request = new LoginRequest("ehgns852", "123123");
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJlaGduczg1MiIsImlhdCI6MTY1MjU1MTQ3MywiZXhwIjoxNjUyNTUzMjczfQ.b1NVO7HODNhEL6_YfIRBFJpRmu1JElErY1LXtDXFJ_I";
        given(userService.login(any(LoginRequest.class))).willReturn(new Token(token));

        //when
        ResultActions result = mockMvc.perform(post("/api/users/login")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8));

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value(token))
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andDo(document("user/login",
                        requestFields(
                                fieldWithPath("username").description("회원 ID"),
                                fieldWithPath("password").description("비밀 번호")
                        ),
                        responseFields(
                                fieldWithPath("accessToken").description("JWT Access Token")
                        )));
        verify(userService, times(1)).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("회원 로그인 - 회원을 찾지 못한 경우 실패")
    void loginFail() throws Exception{
        //given
        LoginRequest request = new LoginRequest("ehgns852", "123123");

        doThrow(new UserNotFoundException("회원을 찾을 수 없습니다."))
                .when(userService).login(any(LoginRequest.class));

        //when
        ResultActions result = mockMvc.perform(post("/api/users/login")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8));

        //then
        result.andExpect(status().isNotFound())
                .andDo(document("user/login/fail"));

        verify(userService, times(1)).login(any(LoginRequest.class));
    }

}