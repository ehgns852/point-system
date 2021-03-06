package com.backend.pointsystem.controller;

import com.backend.pointsystem.dto.request.CreateItemRequest;
import com.backend.pointsystem.dto.request.UpdateItemRequest;
import com.backend.pointsystem.entity.ItemStatus;
import com.backend.pointsystem.exception.PointRatioSettingException;
import com.backend.pointsystem.security.WebSecurityConfig;
import com.backend.pointsystem.security.jwt.JwtAuthenticationFilter;
import com.backend.pointsystem.service.ItemService;
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

import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(controllers = ItemController.class,
        excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = WebSecurityConfig.class)} )
@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
@MockBean(JpaMetamodelMappingContext.class)
class ItemControllerTest {

    @MockBean
    private ItemService itemService;

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
    @DisplayName("?????? ?????? - ??????")
    void createItem() throws Exception {
        //given
        CreateItemRequest request = new CreateItemRequest("??????", 10000, 20, 10,
                "opusm", ItemStatus.SELL);

        given(itemService.createItem(any(CreateItemRequest.class))).willReturn(1L);

        //when
        ResultActions result = mockMvc.perform(post("/api/items")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8)
                .content(objectMapper.writeValueAsString(request)));

        //then
        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.itemId").isNumber())
                .andExpect(jsonPath("$.itemId").value(1L))
                .andDo(document("item/create",
                        requestFields(
                                fieldWithPath("itemName").description("?????????"),
                                fieldWithPath("price").description("?????? ??????"),
                                fieldWithPath("stockQuantity").description("?????? ??????"),
                                fieldWithPath("pointRatio").description("?????? ??????"),
                                fieldWithPath("owner").description("?????????"),
                                fieldWithPath("itemStatus").description("?????? ?????? ??????")),
                        responseFields(
                                fieldWithPath("itemId").description("?????? PK"))
                ));

        verify(itemService, times(1)).createItem(any(CreateItemRequest.class));
    }

    @Test
    @DisplayName("?????? ?????? - ????????? ????????? 0 ?????? 100 ????????? ?????? ??????")
    void createItemFail() throws Exception{
        //given
        CreateItemRequest request = new CreateItemRequest("??????", 10000, 20, 10,
                "opusm", ItemStatus.SELL);

        doThrow(new PointRatioSettingException("????????? ?????? ????????? ???????????? ????????????."))
                .when(itemService).createItem(any(CreateItemRequest.class));

        //when
        ResultActions result = mockMvc.perform(post("/api/items")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8)
                .content(objectMapper.writeValueAsString(request)));

        //then
        result.andExpect(status().isBadRequest())
                .andDo(document("item/create/fail"));

        verify(itemService, times(1)).createItem(any(CreateItemRequest.class));
    }

    @Test
    @DisplayName("?????? ?????? - ??????")
    void updateItem() throws Exception {
        //given
        UpdateItemRequest request = new UpdateItemRequest(1L, "?????????", 1000, 100, 5, "opusm", ItemStatus.SELL);

        //when
        ResultActions result = mockMvc.perform(patch("/api/items")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8)
                .content(objectMapper.writeValueAsString(request)));

        //then
        result.andExpect(status().isNoContent())
                .andDo(document("item/update",
                        requestFields(
                                fieldWithPath("itemId").description("?????? ?????? PK"),
                                fieldWithPath("itemName").description("?????? ??????"),
                                fieldWithPath("price").description("?????? ??????"),
                                fieldWithPath("stockQuantity").description("?????? ??????"),
                                fieldWithPath("pointRatio").description("????????? ?????? ??????"),
                                fieldWithPath("owner").description("?????????"),
                                fieldWithPath("itemStatus").description("?????? ??????"))
                ));
    }

}