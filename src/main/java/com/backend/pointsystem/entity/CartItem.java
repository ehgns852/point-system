package com.backend.pointsystem.entity;

import com.backend.pointsystem.common.BaseEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Entity
@NoArgsConstructor(access = PROTECTED)
@Getter
public class CartItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int count;

    @Column(nullable = false)
    private int totalPrice;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "cart_id")
    private Cart cart;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    @Builder
    public CartItem(int count, int totalPrice, Cart cart, Item item) {
        this.count = count;
        this.totalPrice = totalPrice;
        this.cart = cart;
        this.item = item;
    }

    public static CartItem addItemToCart(Cart cart, Item item, Integer itemCount, int totalPrice) {
        return CartItem.builder()
                .item(item)
                .count(itemCount)
                .cart(cart)
                .totalPrice(totalPrice)
                .build();
    }

    public void updateCartItem(Integer itemCount, int totalPrice) {
        this.count += itemCount;
        this.totalPrice += totalPrice;
    }

}
