package com.backend.pointsystem.controller;

import com.backend.pointsystem.dto.request.CreateUserRequest;
import com.backend.pointsystem.dto.request.LoginRequest;
import com.backend.pointsystem.dto.request.UpdateUserRequest;
import com.backend.pointsystem.dto.response.MyOrderOneResponse;
import com.backend.pointsystem.dto.response.MyOrderResponse;
import com.backend.pointsystem.dto.response.MyPurchaseItemResponse;
import com.backend.pointsystem.dto.response.MyPurchaseResponse;
import com.backend.pointsystem.entity.PaymentMethod;
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
import java.util.List;

import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


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
    @DisplayName("?????? ???????????? - ??????")
    void accountUser() throws Exception {
        //given
        given(userService.signUp(any(CreateUserRequest.class))).willReturn(1L);

        //when
        ResultActions result = mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8)
                .content(objectMapper.writeValueAsString(new CreateUserRequest("??????", "ehgns852", "123123", 100000))));

        //then
        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(1L))
                .andDo(document("user/create",
                        requestFields(
                                fieldWithPath("name").description("?????? ??????"),
                                fieldWithPath("username").description("?????? ID"),
                                fieldWithPath("password").description("????????????"),
                                fieldWithPath("asset").description("?????? ??????")
                        ),
                        responseFields(
                                fieldWithPath("userId").description("?????? PK")
                        )));

        verify(userService, times(1)).signUp(any(CreateUserRequest.class));
    }

    @Test
    @DisplayName("?????? ???????????? - ?????? ID??? ????????? ?????? ??????")
    void accountUserFail() throws Exception {
        //given

        doThrow(new DuplicateUserException("?????? ???????????? ID ?????????."))
                .when(userService).signUp(any(CreateUserRequest.class));

        //when
        ResultActions result = mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8)
                .content(objectMapper.writeValueAsString(new CreateUserRequest("??????", "ehgns852", "123123", 100000))));

        //then
        result.andExpect(status().isBadRequest())
                .andDo(document("user/create/fail"));

        verify(userService, times(1)).signUp(any(CreateUserRequest.class));
    }

    @Test
    @DisplayName("?????? ????????? - ??????")
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
                                fieldWithPath("username").description("?????? ID"),
                                fieldWithPath("password").description("?????? ??????")
                        ),
                        responseFields(
                                fieldWithPath("accessToken").description("JWT Access Token")
                        )));
        verify(userService, times(1)).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("?????? ????????? - ????????? ?????? ?????? ?????? ??????")
    void loginFail() throws Exception{
        //given
        LoginRequest request = new LoginRequest("ehgns852", "123123");

        doThrow(new UserNotFoundException("????????? ?????? ??? ????????????."))
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

    @Test
    @DisplayName("?????? ?????? ?????? ??? ?????? ?????? - ??????")
    void updateUser() throws Exception {
        //given
        UpdateUserRequest request = new UpdateUserRequest("?????????", 10000);

        //when
        ResultActions result = mockMvc.perform(patch("/api/users/")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8)
                .content(objectMapper.writeValueAsString(request)));

        //then
        result.andExpect(status().isNoContent())
                .andDo(document("user/update",
                        requestFields(
                                fieldWithPath("name").description("????????? ??????"),
                                fieldWithPath("asset").description("????????? ??????"))
                        ));
    }

    @Test
    @DisplayName("?????? ????????? ?????? ?????? ?????? - ??????")
    void getMyItem() throws Exception {
        //given
        List<MyPurchaseResponse> myItems = List.of(new MyPurchaseResponse(1L, 1L, "??????", 10000, 2, PaymentMethod.MONEY),
                new MyPurchaseResponse(1L, 2L, "??????", 60000, 10, PaymentMethod.MONEY),
                new MyPurchaseResponse(2L, 3L, "??????", 20000, 4, PaymentMethod.POINT));

        MyPurchaseItemResponse response = new MyPurchaseItemResponse(myItems);

        given(userService.getMyItem()).willReturn(response);

        //when
        ResultActions result = mockMvc.perform(get("/api/users/my-item"));

        //then
        result.andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)))
                .andDo(document("user/my-item",
                        responseFields(
                                fieldWithPath("myPurchaseResponses").description("?????? ????????? ?????? ??????"),
                                fieldWithPath("myPurchaseResponses[].orderId").description("?????? ?????? PK"),
                                fieldWithPath("myPurchaseResponses[].itemId").description("?????? PK"),
                                fieldWithPath("myPurchaseResponses[].itemName").description("?????? ??????"),
                                fieldWithPath("myPurchaseResponses[].totalPrice").description("?????? ??? ??????"),
                                fieldWithPath("myPurchaseResponses[].count").description("?????? ?????? ??????"),
                                fieldWithPath("myPurchaseResponses[].paymentMethod").description("?????? ??????")
                        )));
        verify(userService, times(1)).getMyItem();
    }

    @Test
    @DisplayName("?????? ?????? ?????? - ??????")
    void getMyOrder() throws Exception {
        //given
        List<MyOrderOneResponse> myOrder = List.of(new MyOrderOneResponse(1L, 1L, "??????", PaymentMethod.MONEY, 2, 20000),
                new MyOrderOneResponse(1L, 2L, "??????", PaymentMethod.MONEY, 3, 20000));

        MyOrderResponse response = new MyOrderResponse(myOrder);

        given(userService.getMyOrder(anyLong())).willReturn(response);

        //when
        ResultActions result = mockMvc.perform(get("/api/users/my-order/1"));

        //then
        result.andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)))
                .andDo(document("user/my-order",
                        responseFields(
                                fieldWithPath("myOrderOneResponses").description("?????? ?????? ?????? ??????"),
                                fieldWithPath("myOrderOneResponses[].orderId").description("?????? PK"),
                                fieldWithPath("myOrderOneResponses[].itemId").description("?????? PK"),
                                fieldWithPath("myOrderOneResponses[].itemName").description("?????????"),
                                fieldWithPath("myOrderOneResponses[].paymentMethod").description("?????? ??????"),
                                fieldWithPath("myOrderOneResponses[].count").description("?????? ??????"),
                                fieldWithPath("myOrderOneResponses[].totalPrice").description("??? ??????"))
                ));
        verify(userService, times(1)).getMyOrder(anyLong());
    }

}