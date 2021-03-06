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

    @Test
    @DisplayName("회원 이름 수정 및 자산 충전 - 성공")
    void updateUser() throws Exception {
        //given
        UpdateUserRequest request = new UpdateUserRequest("김도훈", 10000);

        //when
        ResultActions result = mockMvc.perform(patch("/api/users/")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8)
                .content(objectMapper.writeValueAsString(request)));

        //then
        result.andExpect(status().isNoContent())
                .andDo(document("user/update",
                        requestFields(
                                fieldWithPath("name").description("변경할 이름"),
                                fieldWithPath("asset").description("충전할 재산"))
                        ));
    }

    @Test
    @DisplayName("내가 구매한 상품 목록 조회 - 성공")
    void getMyItem() throws Exception {
        //given
        List<MyPurchaseResponse> myItems = List.of(new MyPurchaseResponse(1L, 1L, "우유", 10000, 2, PaymentMethod.MONEY),
                new MyPurchaseResponse(1L, 2L, "계란", 60000, 10, PaymentMethod.MONEY),
                new MyPurchaseResponse(2L, 3L, "찐빵", 20000, 4, PaymentMethod.POINT));

        MyPurchaseItemResponse response = new MyPurchaseItemResponse(myItems);

        given(userService.getMyItem()).willReturn(response);

        //when
        ResultActions result = mockMvc.perform(get("/api/users/my-item"));

        //then
        result.andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)))
                .andDo(document("user/my-item",
                        responseFields(
                                fieldWithPath("myPurchaseResponses").description("내가 구매한 상품 목록"),
                                fieldWithPath("myPurchaseResponses[].orderId").description("상품 주문 PK"),
                                fieldWithPath("myPurchaseResponses[].itemId").description("상품 PK"),
                                fieldWithPath("myPurchaseResponses[].itemName").description("상품 이름"),
                                fieldWithPath("myPurchaseResponses[].totalPrice").description("상품 총 가격"),
                                fieldWithPath("myPurchaseResponses[].count").description("상품 구매 개수"),
                                fieldWithPath("myPurchaseResponses[].paymentMethod").description("결제 방법")
                        )));
        verify(userService, times(1)).getMyItem();
    }

    @Test
    @DisplayName("주문 단건 조회 - 성공")
    void getMyOrder() throws Exception {
        //given
        List<MyOrderOneResponse> myOrder = List.of(new MyOrderOneResponse(1L, 1L, "우유", PaymentMethod.MONEY, 2, 20000),
                new MyOrderOneResponse(1L, 2L, "계란", PaymentMethod.MONEY, 3, 20000));

        MyOrderResponse response = new MyOrderResponse(myOrder);

        given(userService.getMyOrder(anyLong())).willReturn(response);

        //when
        ResultActions result = mockMvc.perform(get("/api/users/my-order/1"));

        //then
        result.andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)))
                .andDo(document("user/my-order",
                        responseFields(
                                fieldWithPath("myOrderOneResponses").description("해당 주문 상품 목록"),
                                fieldWithPath("myOrderOneResponses[].orderId").description("주문 PK"),
                                fieldWithPath("myOrderOneResponses[].itemId").description("상품 PK"),
                                fieldWithPath("myOrderOneResponses[].itemName").description("상품명"),
                                fieldWithPath("myOrderOneResponses[].paymentMethod").description("결제 수단"),
                                fieldWithPath("myOrderOneResponses[].count").description("상품 개수"),
                                fieldWithPath("myOrderOneResponses[].totalPrice").description("총 금액"))
                ));
        verify(userService, times(1)).getMyOrder(anyLong());
    }

}