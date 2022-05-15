package com.backend.pointsystem.service;

import com.backend.pointsystem.dto.request.CreateItemRequest;
import com.backend.pointsystem.dummy.ItemDummy;
import com.backend.pointsystem.entity.Item;
import com.backend.pointsystem.entity.ItemStatus;
import com.backend.pointsystem.exception.PointRatioSettingException;
import com.backend.pointsystem.repository.ItemRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemService itemService;

    @Test
    @DisplayName("상품 생성 - 성공")
    void createItem() {
        //given
        Item item = ItemDummy.itemDummy();

        given(itemRepository.save(any(Item.class))).willReturn(item);

        //when
        Long response = itemService.createItem(new CreateItemRequest("우유", 10000, 20, 10,
                "opusm", ItemStatus.SELL));

        //then
        assertThat(response).isEqualTo(1L);

        verify(itemRepository, times(1)).save(any(Item.class));
    }

    @Test
    @DisplayName("상품 생성 - 포인트 비율이 0 미만 100 초과인 경우 실패")
    void createItemFail() {
        //given
        //when
        //then
        assertThatThrownBy(() -> itemService.createItem(new CreateItemRequest("우유", 10000, 20, 101,
                "opusm", ItemStatus.SELL)))
                .isInstanceOf(PointRatioSettingException.class);

    }

}