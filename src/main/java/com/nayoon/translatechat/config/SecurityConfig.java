package com.nayoon.translatechat.config;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

import com.nayoon.translatechat.config.jwt.JwtAuthenticationFilter;
import com.nayoon.translatechat.config.jwt.JwtService;
import com.nayoon.translatechat.config.oauth.CustomOAuth2UserService;
import com.nayoon.translatechat.config.oauth.handler.CustomAccessDeniedHandler;
import com.nayoon.translatechat.config.oauth.handler.OAuth2AuthenticationFailureHandler;
import com.nayoon.translatechat.config.oauth.handler.OAuth2AuthenticationSuccessHandler;
import com.nayoon.translatechat.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtService jwtService;
  private final CustomOAuth2UserService customOAuth2UserService;
  private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
  private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;
  private final MemberRepository memberRepository;

  // H2 Console에 대한 요청이 Spring Security 필터를 통과하지 않도록 설정 -> h2-console이 enable일 때만 작동
  @Bean
  @ConditionalOnProperty(name = "spring.h2.console.enabled", havingValue = "true")
  public WebSecurityCustomizer configureH2ConsoleEnable() {
    return web -> web.ignoring()
        .requestMatchers(PathRequest.toH2Console());
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable) // csrf 보안 사용 X
        .formLogin(AbstractHttpConfigurer::disable) // formLogin 사용 X
        .sessionManagement(AbstractHttpConfigurer::disable) // session 사용 X
        .headers(h -> h
            .frameOptions(FrameOptionsConfig::disable)
        )
        .httpBasic(AbstractHttpConfigurer::disable)
        // URL 별 권한 관리 옵션
        .authorizeHttpRequests(authorize -> authorize
            .requestMatchers(
                antMatcher("/"),
                antMatcher("/swagger-ui/**"),
                antMatcher("/v3/api-docs/**"),
                antMatcher("/login/**"),
                antMatcher("/oauth2/**")
            ).permitAll()
            .anyRequest().authenticated()
        )
        .oauth2Login(oauth2 -> oauth2
            .authorizationEndpoint(authorization -> authorization
                .baseUri("/oauth2/authorization/**")
            )
            .redirectionEndpoint(redirection -> redirection
                .baseUri("/login/oauth2/callback/**")
            )
            .successHandler(oAuth2AuthenticationSuccessHandler)
            .failureHandler(oAuth2AuthenticationFailureHandler)
            .userInfoEndpoint(userInfo -> userInfo
                .userService(customOAuth2UserService)
            )
        )
        .exceptionHandling(ex -> ex
            .accessDeniedHandler(new CustomAccessDeniedHandler())
        )
    ;

    http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  // JWT Filter
  @Bean
  public JwtAuthenticationFilter jwtAuthenticationFilter() {
    return new JwtAuthenticationFilter(jwtService, memberRepository);
  }

}
