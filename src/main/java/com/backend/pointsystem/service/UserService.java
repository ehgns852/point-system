package com.backend.pointsystem.service;

import com.backend.pointsystem.common.UserUtil;
import com.backend.pointsystem.dto.request.CreateUserRequest;
import com.backend.pointsystem.dto.request.LoginRequest;
import com.backend.pointsystem.dto.request.UpdateUserRequest;
import com.backend.pointsystem.dto.response.MyPurchaseItemResponse;
import com.backend.pointsystem.dto.response.MyPurchaseResponse;
import com.backend.pointsystem.entity.Order;
import com.backend.pointsystem.entity.OrderItem;
import com.backend.pointsystem.entity.User;
import com.backend.pointsystem.exception.DuplicateUserException;
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
            throw new DuplicateUserException("이미 존재하는 ID 입니다.");
        }
    }

    public Token login(LoginRequest request) {

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UserNotFoundException("회원을 찾을 수 없습니다."));

        PasswordEncoder encoder = PasswordUtil.getPasswordEncoder();

        validatePassWord(request, user, encoder);

        return jwtProvider.createToken(user.getUsername());
    }

    private void validatePassWord(LoginRequest request, User user, PasswordEncoder encoder) {
        if (!encoder.matches(request.getPassword(), user.getPassword())) {
            throw new UserNotFoundException("id 와 password 가 일치하지 않습니다.");
        }
    }

    @Transactional
    public void updateUser(UpdateUserRequest request) {

        User user = userUtil.findCurrentUser();

        user.updateUser(request.getName(), request.getAsset());
    }

    /**
     * 내가 구매한 모든 상품 조회
     */
    public MyPurchaseItemResponse getMyItem() {
        User user = userUtil.findCurrentUser();

        List<Order> orders = orderRepository.findByUser(user);

        List<MyPurchaseResponse> myPurchaseItems = getMyPurchaseResponses(orders);

        return new MyPurchaseItemResponse(myPurchaseItems);
    }

    private List<MyPurchaseResponse> getMyPurchaseResponses(List<Order> orders) {

        List<MyPurchaseResponse> myPurchaseItems = new ArrayList<>();

        for (Order order : orders) {
            for (OrderItem orderItem : order.getOrderItems()) {
                MyPurchaseResponse items = new MyPurchaseResponse(order.getId(), orderItem.getItem().getId(), orderItem.getItem().getName(),
                        orderItem.getTotalPrice(), orderItem.getCount(), orderItem.getPaymentMethod());
                myPurchaseItems.add(items);
            }
        }

        return myPurchaseItems;
    }
}
