package com.backend.pointsystem.common;

import com.backend.pointsystem.entity.User;
import com.backend.pointsystem.exception.UserNotFoundException;
import com.backend.pointsystem.repository.UserRepository;
import com.backend.pointsystem.utils.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserUtil {

    private final UserRepository userRepository;

    public User findCurrentUser() {
        return userRepository.findByUsername(SecurityUtil.getCurrentUsername())
                .orElseThrow(() -> new UserNotFoundException("회원을 찾을 수 없습니다."));
    }
}
