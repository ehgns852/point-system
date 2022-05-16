package com.backend.pointsystem.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

@Component
@Slf4j
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        log.info("in EntryPoint");
        Object errorObject = request.getAttribute("IllegalArgumentException");
        if (errorObject != null) {
            log.info("errorObject Not null");
            sendError(response, HttpStatus.UNAUTHORIZED);
        }
        log.info("errorObject is null");
        sendError(response, HttpStatus.FORBIDDEN);
    }

    private void sendError(HttpServletResponse response, HttpStatus httpStatus) throws IOException {
        response.setStatus(httpStatus.value());
        response.setContentType("application/json;charset=utf-8");
        try (OutputStream os = response.getOutputStream()) {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writeValue(os, httpStatus);
            os.flush();
        }
    }
}
