package com.backend.pointsystem.security.jwt;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;

import static lombok.AccessLevel.PRIVATE;

@Slf4j
@NoArgsConstructor(access = PRIVATE)
public class AuthorizationExtractor {

    public static final String AUTHORIZATION = "Authorization";
    public static String BEARER_TYPE = "Bearer ";

    public static String extract(HttpServletRequest request) {
        String header = request.getHeader(AUTHORIZATION);
        log.info("header = {}", header);
            if (header != null && header.startsWith(BEARER_TYPE)) {
                String token = header.substring(7);
                log.info("authHeaderValue = {}", token);
                return token;
            }
        return null;
    }
}
