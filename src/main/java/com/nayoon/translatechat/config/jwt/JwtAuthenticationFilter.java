package com.nayoon.translatechat.config.jwt;

import com.nayoon.translatechat.config.oauth.CustomUserDetails;
import com.nayoon.translatechat.entity.Member;
import com.nayoon.translatechat.repository.MemberRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final String NO_CHECK_URL = "/login";

  private final JwtService jwtService;
  private final MemberRepository memberRepository;

  private GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain
  ) throws ServletException, IOException {

    if (request.getRequestURI().equals(NO_CHECK_URL)) {
      filterChain.doFilter(request, response); // 다음 필터 호출
      return;
    }

    // 헤더에서 RefreshToken 추출
    String refreshToken = jwtService.extractRefreshToken(request)
        .filter(jwtService::isValidToken)
        .orElse(null);

    // DB RefreshToken과 일치하는지 확인 후, AccessToken 재발급
    if (refreshToken != null) {
      checkRefreshTokenAndRecreateTokens(response, refreshToken);
    } else {
      checkAccessTokenAndAuthentication(request, response, filterChain);
    }
  }

  // RefreshToken으로 유저 정보 찾기 및 AccessToken/RefreshToken 재발급 메서드
  public void checkRefreshTokenAndRecreateTokens(HttpServletResponse response,
      String refreshToken) {
    Member member = memberRepository.findByRefreshToken(refreshToken).orElse(null);

    if (member != null) {
      String newRefreshToken = jwtService.recreateRefreshToken(member);
      jwtService.sendAccessAndRefreshToken(response, jwtService.createAccessToken(member),
          newRefreshToken);
    }
  }

  // AccessToken 확인 및 인증 처리 메서드
  public void checkAccessTokenAndAuthentication(HttpServletRequest request,
      HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
    jwtService.extractAccessToken(request)
        .filter(jwtService::isValidToken)
        .ifPresent(accessToken -> {
          String email = jwtService.extractEmail(accessToken);
          memberRepository.findByEmail(email)
              .ifPresent(this::saveAuthentication);
        });

    filterChain.doFilter(request, response);
  }

  // Authentication 객체에 대한 인증 처리 메서드
  public void saveAuthentication(Member member) {
    CustomUserDetails customUserDetails = CustomUserDetails.create(member);

    Authentication authentication = new UsernamePasswordAuthenticationToken(customUserDetails, null,
        authoritiesMapper.mapAuthorities(customUserDetails.getAuthorities()));

    SecurityContextHolder.getContext().setAuthentication(authentication);
  }

}
