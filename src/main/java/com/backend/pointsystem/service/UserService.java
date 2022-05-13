package com.backend.pointsystem.service;

import com.backend.pointsystem.dto.request.CreateUserRequest;
import com.backend.pointsystem.dto.request.LoginRequest;
import com.backend.pointsystem.entity.User;
import com.backend.pointsystem.exception.UserNotFoundException;
import com.backend.pointsystem.repository.UserRepository;
import com.backend.pointsystem.security.jwt.JwtProvider;
import com.backend.pointsystem.security.jwt.Token;
import com.backend.pointsystem.utils.PasswordUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;

    @Transactional
    public Long signUp(CreateUserRequest request) {

        User user = User.createUser(request.getName(), request.getUsername(), request.getPassword(), request.getAsset());

        return userRepository.save(user).getId();
    }

    public Token login(LoginRequest request) {

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UserNotFoundException("회원을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        PasswordEncoder encoder = PasswordUtil.getPasswordEncoder();

        if (!encoder.matches(request.getPassword(), user.getPassword())) {
            throw new UserNotFoundException("회원을 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
        }

        return jwtProvider.createToken(user.getUsername());
    }
}
