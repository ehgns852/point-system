package com.backend.pointsystem.repository;

import com.backend.pointsystem.dto.response.MyPurchaseResponse;
import com.backend.pointsystem.entity.Order;
import com.backend.pointsystem.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query("select new com.backend.pointsystem.dto.response.MyPurchaseResponse(o.id, i.id, i.name, oi.totalPrice, oi.count, oi.paymentMethod) from Order o inner join o.user u inner join o.orderItems oi inner join oi.item i where u.id = :userId")
    List<MyPurchaseResponse> findMyOrders(@Param("userId") Long userId);

    Optional<Order> findByIdAndUser(Long orderId, User user);
}
