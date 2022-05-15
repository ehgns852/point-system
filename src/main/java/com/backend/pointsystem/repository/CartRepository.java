package com.backend.pointsystem.repository;

import com.backend.pointsystem.entity.Cart;
import com.backend.pointsystem.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUser(User user);
}
