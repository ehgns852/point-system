package com.backend.pointsystem.service;

import com.backend.pointsystem.dto.request.CreateUserRequest;
import com.backend.pointsystem.dto.request.LoginRequest;
import com.backend.pointsystem.dummy.UserDummy;
import com.backend.pointsystem.entity.User;
import com.backend.pointsystem.exception.DuplicateUserException;
import com.backend.pointsystem.exception.UserNotFoundException;
import com.backend.pointsystem.repository.UserRepository;
import com.backend.pointsystem.security.jwt.JwtProvider;
import com.backend.pointsystem.security.jwt.Token;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

}