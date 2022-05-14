package com.backend.pointsystem.entity;

import com.backend.pointsystem.common.BaseEntity;
import com.backend.pointsystem.exception.NotEnoughStockException;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import java.util.ArrayList;
import java.util.List;

import static javax.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Entity
@NoArgsConstructor(access = PROTECTED)
@Getter
public class Item extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "product_id")
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer price;

    private int stockQuantity;

    private int pointRatio;

    @Column(nullable = false)
    private String owner;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ItemStatus itemStatus;

    @OneToMany(mappedBy = "item")
    private List<OrderItem> orderItems = new ArrayList<>();

    @Builder
    public Item(String name, int price, int stockQuantity, int pointRatio, String owner, ItemStatus itemStatus) {
        this.name = name;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.pointRatio = pointRatio;
        this.owner = owner;
        this.itemStatus = itemStatus;
    }


    /**
     * 생성 메서드
     */
    public static Item createItem(String name, int price, int stockQuantity, int pointRatio, String owner, ItemStatus itemStatus) {
        return Item.builder()
                .name(name)
                .price(price)
                .stockQuantity(stockQuantity)
                .pointRatio(pointRatio)
                .owner(owner)
                .itemStatus(itemStatus)
                .build();
    }

    public void removeStock(int count) {
        int restStock = this.stockQuantity - count;

        if (restStock == 0) {
            this.itemStatus = ItemStatus.SOLD_OUT;
        }
        if (restStock < 0) {
            throw new NotEnoughStockException("재고 수량이 부족합니다.");
        }

        this.stockQuantity = restStock;
    }
}
