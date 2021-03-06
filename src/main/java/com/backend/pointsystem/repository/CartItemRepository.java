package com.backend.pointsystem.repository;

import com.backend.pointsystem.entity.Cart;
import com.backend.pointsystem.entity.CartItem;
import com.backend.pointsystem.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByCartAndItem(Cart cart, Item item);

    List<CartItem> findByCart(Cart cart);
}
