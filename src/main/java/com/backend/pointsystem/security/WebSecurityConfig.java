package com.backend.pointsystem.security;

import com.backend.pointsystem.security.jwt.JwtAuthenticationFilter;
import com.backend.pointsystem.utils.PasswordUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .cors().and()
                .httpBasic()
                .disable()
                .csrf()
                .disable()
                .headers()
                .frameOptions()
                .disable();

        http
                .authorizeRequests()
                .anyRequest()
                .authenticated()
                .and()
//                                .exceptionHandling()
//                                        .authenticationEntryPoint()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .formLogin().disable();

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring()
                .antMatchers(HttpMethod.POST, "/api/users")
                .antMatchers(HttpMethod.POST, "/api/users/login")
                .antMatchers(HttpMethod.POST, "/api/items");
    }

    @Bean
    public PasswordEncoder encode() {
        return PasswordUtil.getPasswordEncoder();
    }
}
