package com.backend.pointsystem.repository;

import com.backend.pointsystem.entity.Order;
import com.backend.pointsystem.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser(User user);
}
