package com.nayoon.translatechat.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  // H2 Console에 대한 요청이 Spring Security 필터를 통과하지 않도록 설정 -> h2-console이 enable일 때만 작동
  @Bean
  @ConditionalOnProperty(name = "spring.h2.console.enabled",havingValue = "true")
  public WebSecurityCustomizer configureH2ConsoleEnable() {
    return web -> web.ignoring()
        .requestMatchers(PathRequest.toH2Console());
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable) // csrf 보안 사용 X
        // 세션 사용하지 않으므로 STATELESS 로 설정
        .sessionManagement(sessionManagement ->
            sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        // URL 별 권한 관리 옵션
        .authorizeHttpRequests(authorize -> authorize
            .anyRequest().authenticated()
        )
    ;

    return http.build();
  }

}
