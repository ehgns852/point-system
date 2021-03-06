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
    @DisplayName("??????????????? ?????? ?????? - ??????")
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
                                fieldWithPath("cartRequests").description("?????? ?????????"),
                                fieldWithPath("cartRequests[].itemId").description("?????? PK"),
                                fieldWithPath("cartRequests[].itemCount").description("??????????????? ?????? ?????? ??????")),
                        responseFields(
                                fieldWithPath("cartId").description("?????? ????????? ???????????? PK"))
                ));

        verify(cartService, times(1)).addItemToCart(any(AddItemToCartRequest.class));
    }

    @Test
    @DisplayName("???????????? ?????? ?????? ?????? - ??????")
    void deleteItemToCart() throws Exception {
        //given
        //when
        ResultActions result = mockMvc.perform(delete("/api/carts/1"));

        //then
        result.andExpect(status().isNoContent())
                .andDo(document("cart/delete"));
    }

    @Test
    @DisplayName("???????????? ?????? ?????? ?????? - ??????????????? ?????? ????????? ???????????? ?????? ?????? ??????")
    void deleteItemToCartFail() throws Exception {
        //given

        doThrow(new CartItemNotFountException("??????????????? ????????? ???????????? ????????????."))
                .when(cartService).deleteItemToCart(anyLong());

        //when
        ResultActions result = mockMvc.perform(delete("/api/carts/1"));

        //then
        result.andExpect(status().isNotFound())
                .andDo(document("cart/delete/fail"));
    }

    @Test
    @DisplayName("??? ???????????? ?????? ?????? ?????? - ??????")
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
                                fieldWithPath("paymentMethod").description("?????? ??????")),
                        responseFields(
                                fieldWithPath("orderId").description("?????? PK"))
                ));

        verify(cartService, times(1)).myCartBuyAll(any(BuyAllRequest.class));
    }

    @Test
    @DisplayName("??? ???????????? ?????? ?????? ?????? - ?????? ????????? ????????? ?????? ??????")
    void myCartBuyAllFail() throws Exception {
        //given
        BuyAllRequest request = new BuyAllRequest(PaymentMethod.MONEY);

        doThrow(new NotEnoughStockException("?????? ?????? ????????? ???????????????."))
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
    @DisplayName("??? ???????????? ?????? ?????? ?????? - ????????? ?????? OR ???????????? ????????? ?????? ??????")
    void myCartBuyAllFail2() throws Exception{
        //given
        BuyAllRequest request = new BuyAllRequest(PaymentMethod.MONEY);

        doThrow(new LockOfMoneyException("????????? ?????? OR ???????????? ???????????????."))
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