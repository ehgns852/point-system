package com.backend.pointsystem.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordUtil {

    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private PasswordUtil() {
    }

    public static PasswordEncoder getPasswordEncoder() {
        return passwordEncoder;
    }

    public static String encode(String password) {
        return passwordEncoder.encode(password);
    }
}
