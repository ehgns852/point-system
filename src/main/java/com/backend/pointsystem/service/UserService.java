package com.backend.pointsystem.service;

import com.backend.pointsystem.common.UserUtil;
import com.backend.pointsystem.dto.request.CreateUserRequest;
import com.backend.pointsystem.dto.request.LoginRequest;
import com.backend.pointsystem.dto.request.UpdateUserRequest;
import com.backend.pointsystem.dto.response.MyOrderOneResponse;
import com.backend.pointsystem.dto.response.MyOrderResponse;
import com.backend.pointsystem.dto.response.MyPurchaseItemResponse;
import com.backend.pointsystem.dto.response.MyPurchaseResponse;
import com.backend.pointsystem.entity.Order;
import com.backend.pointsystem.entity.OrderItem;
import com.backend.pointsystem.entity.User;
import com.backend.pointsystem.exception.DuplicateUserException;
import com.backend.pointsystem.exception.OrderNotFountException;
import com.backend.pointsystem.exception.UserNotFoundException;
import com.backend.pointsystem.repository.OrderRepository;
import com.backend.pointsystem.repository.UserRepository;
import com.backend.pointsystem.security.jwt.JwtProvider;
import com.backend.pointsystem.security.jwt.Token;
import com.backend.pointsystem.utils.PasswordUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final UserUtil userUtil;
    private final OrderRepository orderRepository;

    @Transactional
    public Long signUp(CreateUserRequest request) {

        validateDuplicateUser(request.getUsername());

        User user = User.createUser(request.getName(), request.getUsername(), request.getPassword(), request.getAsset());

        return userRepository.save(user).getId();
    }

    private void validateDuplicateUser(String username) {

        Optional<User> findUser = userRepository.findByUsername(username);

        if (findUser.isPresent()) {
            throw new DuplicateUserException("?????? ???????????? ID ?????????.");
        }
    }

    public Token login(LoginRequest request) {

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UserNotFoundException("????????? ?????? ??? ????????????."));

        PasswordEncoder encoder = PasswordUtil.getPasswordEncoder();

        validatePassWord(request, user, encoder);

        return jwtProvider.createToken(user.getUsername());
    }

    private void validatePassWord(LoginRequest request, User user, PasswordEncoder encoder) {
        if (!encoder.matches(request.getPassword(), user.getPassword())) {
            throw new UserNotFoundException("id ??? password ??? ???????????? ????????????.");
        }
    }

    @Transactional
    public void updateUser(UpdateUserRequest request) {

        User user = userUtil.findCurrentUser();

        user.updateUser(request.getName(), request.getAsset());
    }

    /**
     * ?????? ????????? ?????? ?????? ??????
     */
    public MyPurchaseItemResponse getMyItem() {
        User user = userUtil.findCurrentUser();

        return new MyPurchaseItemResponse(orderRepository.findMyOrders(user.getId()));
    }

    /**
     * ?????? ?????? ??????
     */
    public MyOrderResponse getMyOrder(Long orderId) {
        User user = userUtil.findCurrentUser();

        Order order = orderRepository.findByIdAndUser(orderId, user)
                .orElseThrow(() -> new OrderNotFountException("?????? ????????? ?????? ???????????????."));

        List<MyOrderOneResponse> result = order.getOrderItems().stream()
                .map(orderItem -> new MyOrderOneResponse(order.getId(), orderItem.getItem().getId(),
                        orderItem.getItem().getName(), orderItem.getPaymentMethod(), orderItem.getCount(), orderItem.getTotalPrice()))
                .collect(Collectors.toList());

        return new MyOrderResponse(result);
    }
}
