package com.backend.pointsystem.repository;

import com.backend.pointsystem.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
