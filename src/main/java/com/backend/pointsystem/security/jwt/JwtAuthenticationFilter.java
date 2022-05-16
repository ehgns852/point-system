package com.backend.pointsystem.security.jwt;

import com.backend.pointsystem.entity.User;
import com.backend.pointsystem.exception.UserNotFoundException;
import com.backend.pointsystem.repository.UserRepository;
import com.backend.pointsystem.security.auth.PrincipleDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider provider;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        log.info("in JwtAuthenticationFilter");
        try {
            String accessToken = AuthorizationExtractor.extract(request);
            log.info("accessToken = {}", accessToken);
            if (StringUtils.hasText(accessToken) && provider.validateToken(accessToken)) {

                String username = provider.getPayload(accessToken);

                User user = userRepository.findByUsername(username)
                        .orElseThrow(() -> new UserNotFoundException("회원을 찾을 수 없습니다."));

                PrincipleDetails principleDetails = new PrincipleDetails(user);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(principleDetails, null, principleDetails.getAuthorities());

                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.info("Authentication = {}", authentication.getName());
            }
        } catch (IllegalArgumentException e) {
            log
                    .error("IllegalArgumentException!");
            request.setAttribute("IllegalArgumentException", e);
        }
        chain.doFilter(request,response);
    }
}
