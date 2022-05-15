package com.backend.pointsystem.dummy;

import com.backend.pointsystem.entity.Item;
import com.backend.pointsystem.entity.ItemStatus;

public class ItemDummy {

    public static Item itemDummy() {
        return Item.builder()
                .id(1L)
                .name("우유")
                .price(10000)
                .itemStatus(ItemStatus.SELL)
                .pointRatio(10)
                .owner("opusm")
                .stockQuantity(100)
                .build();
    }
    public static Item itemDummy2() {
        return Item.builder()
                .id(2L)
                .name("식빵")
                .price(20000)
                .itemStatus(ItemStatus.SELL)
                .pointRatio(5)
                .owner("opusm")
                .stockQuantity(40)
                .build();
    }
}
