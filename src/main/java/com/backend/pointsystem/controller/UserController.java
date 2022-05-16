package com.backend.pointsystem.controller;

import com.backend.pointsystem.dto.request.CreateUserRequest;
import com.backend.pointsystem.dto.request.LoginRequest;
import com.backend.pointsystem.dto.request.UpdateUserRequest;
import com.backend.pointsystem.dto.response.CreateUserResponse;
import com.backend.pointsystem.security.jwt.Token;
import com.backend.pointsystem.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<CreateUserResponse> signUp(@Validated @RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(new CreateUserResponse(userService.signUp(request)));
    }

    @PostMapping("/login")
    public ResponseEntity<Token> login(@Validated @RequestBody LoginRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }

    /**
     * 회원 이름 변경 및 자산 충전 (기존 Asset + request.asset)
     */
    @PatchMapping
    public ResponseEntity<Void> updateUser(@RequestBody UpdateUserRequest request) {
        userService.updateUser(request);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
