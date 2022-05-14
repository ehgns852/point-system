package com.backend.pointsystem.controller;

import com.backend.pointsystem.dto.request.CreateUserRequest;
import com.backend.pointsystem.dto.request.LoginRequest;
import com.backend.pointsystem.dto.response.CreateUserResponse;
import com.backend.pointsystem.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity login(@Validated @RequestBody LoginRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }

}
