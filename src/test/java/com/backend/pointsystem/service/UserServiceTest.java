package com.backend.pointsystem.service;

import com.backend.pointsystem.common.UserUtil;
import com.backend.pointsystem.dto.request.CreateUserRequest;
import com.backend.pointsystem.dto.request.LoginRequest;
import com.backend.pointsystem.dto.request.UpdateUserRequest;
import com.backend.pointsystem.dto.response.MyPurchaseItemResponse;
import com.backend.pointsystem.dummy.ItemDummy;
import com.backend.pointsystem.dummy.UserDummy;
import com.backend.pointsystem.entity.*;
import com.backend.pointsystem.exception.DuplicateUserException;
import com.backend.pointsystem.exception.UserNotFoundException;
import com.backend.pointsystem.repository.OrderRepository;
import com.backend.pointsystem.repository.UserRepository;
import com.backend.pointsystem.security.jwt.JwtProvider;
import com.backend.pointsystem.security.jwt.Token;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private UserUtil userUtil;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("회원 가입 - 성공")
    void signUp() {
        //given
        User user = UserDummy.dummyUser();

        given(userRepository.findByUsername(anyString())).willReturn(Optional.empty());
        given(userRepository.save(any(User.class))).willReturn(user);

        //when
        Long response = userService.signUp(new CreateUserRequest("김도훈", "ehgns852", "123123", 1000000));

        //then
        assertThat(response).isEqualTo(1L);

        verify(userRepository, times(1)).findByUsername(anyString());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("회원 가입 - 회원 ID가 중복된 경우 실패")
    void signUpFail() {
        //given
        User user = UserDummy.dummyUser();
        given(userRepository.findByUsername(anyString())).willReturn(Optional.of(user));

        //when
        assertThatThrownBy(() -> userService.signUp(new CreateUserRequest("김도훈", "ehgns852", "123123", 1000000)))
                .isInstanceOf(DuplicateUserException.class);

        //then
        verify(userRepository, times(1)).findByUsername(anyString());
    }

    @Test
    @DisplayName("회원 로그인 - 성공")
    void login() {
        //given
        User user = UserDummy.dummyUser();
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJlaGduczg1MiIsImlhdCI6MTY1MjU1MTQ3MywiZXhwIjoxNjUyNTUzMjczfQ.b1NVO7HODNhEL6_YfIRBFJpRmu1JElErY1LXtDXFJ_I";

        given(userRepository.findByUsername(anyString())).willReturn(Optional.of(user));
        given(jwtProvider.createToken(anyString())).willReturn(new Token(token));

        //when
        Token response = userService.login(new LoginRequest("ehgns852", "123123"));

        //then
        assertThat(response.getAccessToken()).isEqualTo(token);

        verify(userRepository, times(1)).findByUsername(anyString());
        verify(jwtProvider, times(1)).createToken(anyString());
    }

    @Test
    @DisplayName("회원 로그인 - 비밀 번호가 일치하지 않은 경우 실패")
    void loginFail() {
        //given
        User user = UserDummy.dummyUser();
        given(userRepository.findByUsername(anyString())).willReturn(Optional.of(user));

        //when
        assertThatThrownBy(() -> userService.login(new LoginRequest("ehgns852", "11111")))
                .isInstanceOf(UserNotFoundException.class);

        //then
        verify(userRepository, times(1)).findByUsername(anyString());
    }

    @Test
    @DisplayName("회원 이름 수정 및 자산 충전 - 성공")
    void updateUser() {
        //given
        User user = UserDummy.dummyUser();
        given(userUtil.findCurrentUser()).willReturn(user);

        //when
        userService.updateUser(new UpdateUserRequest("김도훈", 100000));

        //then
        assertThat(user.getAsset()).isEqualTo(10100000);

        verify(userUtil, times(1)).findCurrentUser();
    }

    @Test
    @DisplayName("내가 주문한 상품 전체 조회 - 성공")
    void getMyItem() {
        //given
        User user = UserDummy.dummyUser();

        Item item1 = ItemDummy.itemDummy();
        Item item2 = ItemDummy.itemDummy2();

        List<OrderItem> orderItem = List.of(OrderItem.createOrderItem(item1, PaymentMethod.MONEY, 100000, 3),
                OrderItem.createOrderItem(item2, PaymentMethod.MONEY, 1000, 3));

        List<Order> order = List.of(Order.createOrder(user, orderItem));

        given(userUtil.findCurrentUser()).willReturn(user);
        given(orderRepository.findByUser(any(User.class))).willReturn(order);

        //when
        MyPurchaseItemResponse response = userService.getMyItem();

        //then
        assertThat(response.getMyPurchaseResponses()).extracting("itemName")
                .containsExactly("우유", "식빵");
        assertThat(response.getMyPurchaseResponses()).extracting("totalPrice")
                .containsExactly(100000, 1000);

        verify(userUtil, times(1)).findCurrentUser();
        verify(orderRepository, times(1)).findByUser(any(User.class));
    }

}