package com.backend.pointsystem.controller;

import com.backend.pointsystem.dto.request.AddItemToCartRequest;
import com.backend.pointsystem.dto.request.BuyAllRequest;
import com.backend.pointsystem.dto.request.CartRequest;
import com.backend.pointsystem.entity.PaymentMethod;
import com.backend.pointsystem.exception.CartItemNotFountException;
import com.backend.pointsystem.exception.LockOfMoneyException;
import com.backend.pointsystem.exception.NotEnoughStockException;
import com.backend.pointsystem.security.WebSecurityConfig;
import com.backend.pointsystem.security.jwt.JwtAuthenticationFilter;
import com.backend.pointsystem.service.CartService;
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

import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(controllers = CartController.class,
        excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = WebSecurityConfig.class)} )
@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
@MockBean(JpaMetamodelMappingContext.class)
class CartControllerTest {

    @MockBean
    private CartService cartService;

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
    @DisplayName("장바구니에 상품 담기 - 성공")
    void addItemToCart() throws Exception {
        //given
        List<CartRequest> cartRequests = List.of(new CartRequest(1L, 3),
                new CartRequest(2L, 4));

        AddItemToCartRequest request = new AddItemToCartRequest(cartRequests);

        given(cartService.addItemToCart(any(AddItemToCartRequest.class))).willReturn(1L);

        //when
        ResultActions result = mockMvc.perform(post("/api/carts")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8)
                .content(objectMapper.writeValueAsString(request)));

        //then
        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.cartId").value(1L))
                .andDo(document("cart/create",
                        requestFields(
                                fieldWithPath("cartRequests").description("상품 리스트"),
                                fieldWithPath("cartRequests[].itemId").description("상품 PK"),
                                fieldWithPath("cartRequests[].itemCount").description("장바구니에 담을 상품 개수")),
                        responseFields(
                                fieldWithPath("cartId").description("해당 유저의 장바구니 PK"))
                ));

        verify(cartService, times(1)).addItemToCart(any(AddItemToCartRequest.class));
    }

    @Test
    @DisplayName("장바구니 해당 상품 삭제 - 성공")
    void deleteItemToCart() throws Exception {
        //given
        //when
        ResultActions result = mockMvc.perform(delete("/api/carts/1"));

        //then
        result.andExpect(status().isNoContent())
                .andDo(document("cart/delete"));
    }

    @Test
    @DisplayName("장바구니 해당 상품 삭제 - 장바구니에 해당 상품이 존재하지 않은 경우 실패")
    void deleteItemToCartFail() throws Exception {
        //given

        doThrow(new CartItemNotFountException("장바구니에 상품이 존재하지 않습니다."))
                .when(cartService).deleteItemToCart(anyLong());

        //when
        ResultActions result = mockMvc.perform(delete("/api/carts/1"));

        //then
        result.andExpect(status().isNotFound())
                .andDo(document("cart/delete/fail"));
    }

    @Test
    @DisplayName("내 장바구니 항목 전체 구매 - 성공")
    void myCartBuyAll() throws Exception {
        //given
        BuyAllRequest request = new BuyAllRequest(PaymentMethod.MONEY);

        given(cartService.myCartBuyAll(any(BuyAllRequest.class))).willReturn(1L);

        //when
        ResultActions result = mockMvc.perform(post("/api/carts/buy-all")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8)
                .content(objectMapper.writeValueAsString(request)));

        //then
        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value(1L))
                .andDo(document("cart/buy-all",
                        requestFields(
                                fieldWithPath("paymentMethod").description("결제 수단")),
                        responseFields(
                                fieldWithPath("orderId").description("주문 PK"))
                ));

        verify(cartService, times(1)).myCartBuyAll(any(BuyAllRequest.class));
    }

    @Test
    @DisplayName("내 장바구니 항목 전체 구매 - 상품 재고가 부족한 경우 실패")
    void myCartBuyAllFail() throws Exception {
        //given
        BuyAllRequest request = new BuyAllRequest(PaymentMethod.MONEY);

        doThrow(new NotEnoughStockException("해당 상품 재고가 부족합니다."))
                .when(cartService).myCartBuyAll(any(BuyAllRequest.class));

        //when
        ResultActions result = mockMvc.perform(post("/api/carts/buy-all")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8)
                .content(objectMapper.writeValueAsString(request)));

        //then
        result.andExpect(status().isBadRequest())
                .andDo(document("cart/buy-all/fail"));
    }

    @Test
    @DisplayName("내 장바구니 항목 전체 구매 - 보유한 자산 OR 포인트가 부족한 경우 실패")
    void myCartBuyAllFail2() throws Exception{
        //given
        BuyAllRequest request = new BuyAllRequest(PaymentMethod.MONEY);

        doThrow(new LockOfMoneyException("보유한 자산 OR 포인트가 부족합니다."))
                .when(cartService).myCartBuyAll(any(BuyAllRequest.class));

        //when
        ResultActions result = mockMvc.perform(post("/api/carts/buy-all")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8)
                .content(objectMapper.writeValueAsString(request)));

        //then
        result.andExpect(status().isBadRequest())
                .andDo(document("cart/buy-all/fail2"));
    }

}