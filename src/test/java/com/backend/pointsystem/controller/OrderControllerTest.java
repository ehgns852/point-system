package com.backend.pointsystem.controller;

import com.backend.pointsystem.dto.request.CreateOrderRequest;
import com.backend.pointsystem.dto.request.LoginRequest;
import com.backend.pointsystem.dto.request.OrderRequest;
import com.backend.pointsystem.entity.PaymentMethod;
import com.backend.pointsystem.exception.LockOfMoneyException;
import com.backend.pointsystem.exception.NotEnoughStockException;
import com.backend.pointsystem.exception.UserNotFoundException;
import com.backend.pointsystem.security.WebSecurityConfig;
import com.backend.pointsystem.security.jwt.JwtAuthenticationFilter;
import com.backend.pointsystem.service.OrderService;
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
import org.springframework.restdocs.payload.PayloadDocumentation;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(controllers = OrderController.class,
        excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = WebSecurityConfig.class)} )
@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
@MockBean(JpaMetamodelMappingContext.class)
class OrderControllerTest {

    @MockBean
    private OrderService orderService;

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
    @DisplayName("회원 주문 - 성공")
    void createOrder() throws Exception {
        //given
        List<OrderRequest> orderRequest = List.of(new OrderRequest(1L, 3),
                new OrderRequest(2L, 4),
                new OrderRequest(3L, 5));

        CreateOrderRequest request = new CreateOrderRequest(orderRequest, PaymentMethod.MONEY);

        given(orderService.createOrder(any(CreateOrderRequest.class))).willReturn(1L);

        //when
        ResultActions result = mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8)
                .content(objectMapper.writeValueAsString(request)));

        //then
        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value(1L))
                .andDo(document("order/create",
                        requestFields(
                                fieldWithPath("orderRequests").description("주문 리스트"),
                                fieldWithPath("orderRequests[].itemId").description("상품 PK"),
                                fieldWithPath("orderRequests[].itemCount").description("주문 상품 개수"),
                                fieldWithPath("paymentMethod").description("결제 수단")),
                        responseFields(
                                fieldWithPath("orderId").description("주문 PK"))
                ));

        verify(orderService, times(1)).createOrder(any(CreateOrderRequest.class));
    }

    @Test
    @DisplayName("회원 주문 - 재고 수량이 부족할 경우 실패")
    void createOrderFail() throws Exception{
        //given
        List<OrderRequest> orderRequest = List.of(new OrderRequest(1L, 3),
                new OrderRequest(2L, 4),
                new OrderRequest(3L, 5));

        CreateOrderRequest request = new CreateOrderRequest(orderRequest, PaymentMethod.MONEY);

        doThrow(new NotEnoughStockException("재고 수량이 부족합니다."))
                .when(orderService).createOrder(any(CreateOrderRequest.class));

        //when
        ResultActions result = mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8)
                .content(objectMapper.writeValueAsString(request)));

        //then
        result.andExpect(status().isBadRequest())
                .andDo(document("order/create/fail"));

        verify(orderService, times(1)).createOrder(any(CreateOrderRequest.class));
    }

    @Test
    @DisplayName("회원 주문 - 재산 OR 포인트가 부족할 경우 실패")
    void createOrderFail2() throws Exception{
        //given
        List<OrderRequest> orderRequest = List.of(new OrderRequest(1L, 3),
                new OrderRequest(2L, 4),
                new OrderRequest(3L, 5));

        CreateOrderRequest request = new CreateOrderRequest(orderRequest, PaymentMethod.MONEY);

        doThrow(new LockOfMoneyException("자산 OR 포인트가 부족합니다."))
                .when(orderService).createOrder(any(CreateOrderRequest.class));

        //when
        ResultActions result = mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8)
                .content(objectMapper.writeValueAsString(request)));

        //then
        result.andExpect(status().isBadRequest())
                .andDo(document("order/create/fail2"));

        verify(orderService, times(1)).createOrder(any(CreateOrderRequest.class));
    }
}