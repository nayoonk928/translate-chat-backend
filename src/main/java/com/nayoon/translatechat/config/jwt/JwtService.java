package com.nayoon.translatechat.config.jwt;

import com.nayoon.translatechat.entity.Member;
import com.nayoon.translatechat.exception.CustomException;
import com.nayoon.translatechat.exception.ErrorCode;
import com.nayoon.translatechat.repository.MemberRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import java.util.Set;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Getter
@Service
@RequiredArgsConstructor
public class JwtService {

  @Value("${jwt.secretKey}")
  private String secretKey;

  @Value("${jwt.access.expiration}")
  private Long accessTokenExpirationPeriod;

  @Value("${jwt.refresh.expiration}")
  private Long refreshTokenExpirationPeriod;

  @Value("${jwt.access.header}")
  private String accessHeader;

  @Value("${jwt.refresh.header}")
  private String refreshHeader;

  private static final String TOKEN_HEADER = "Authorization";
  private static final String TOKEN_PREFIX = "Bearer ";
  private static final String ACCESS_TOKEN_SUBJECT = "AccessToken";
  private static final String REFRESH_TOKEN_SUBJECT = "RefreshToken";

  private final MemberRepository memberRepository;

  public String createAccessToken(Member member) {
    Date now = new Date();
    return Jwts.builder()
        .setExpiration(new Date(now.getTime() + accessTokenExpirationPeriod))
        .setIssuedAt(now)
        .setSubject(ACCESS_TOKEN_SUBJECT)
        .claim("email", member.getEmail())
        .claim("id", member.getId())
        .claim("nickname", member.getNickname())
        .signWith(SignatureAlgorithm.HS256, secretKey)
        .compact();
  }

  // RefreshToken의 claim에는 아무것도 저장하지 않음
  public String createRefreshToken() {
    Date now = new Date();
    return Jwts.builder()
        .setExpiration(new Date(now.getTime() + refreshTokenExpirationPeriod))
        .setIssuedAt(now)
        .setSubject(REFRESH_TOKEN_SUBJECT)
        .signWith(SignatureAlgorithm.HS256, secretKey)
        .compact();
  }

  // AccessToken으로 인증 정보 가져오기
  public Authentication getAuthentication(String accessToken) {
    Claims claims = extractClaims(accessToken);
    Set<SimpleGrantedAuthority> authorities = Collections.singleton(
        new SimpleGrantedAuthority("ROLE_USER"));

    return new UsernamePasswordAuthenticationToken(new User(claims.getSubject(), "", authorities),
        accessToken, authorities);
  }

  // 토큰 유효성 검사
  public boolean isValidToken(String token) {
    try {
      Jwts.parser()
          .setSigningKey(secretKey)
          .parseClaimsJws(token);
      return true;
    } catch (Exception e) {
      log.error("유효하지 않은 토큰입니다. {}", e.getMessage());
      return false;
    }
  }

  // AccessToken 헤더에 실어서 보내기
  public void sendAccessToken(HttpServletResponse response, String accessToken) {
    response.setStatus(HttpServletResponse.SC_OK);
    response.setHeader(accessHeader, accessToken);
    log.info("재발급된 Access Token : {}", accessToken);
  }

  // AccessToken + RefreshToken 헤더에 실어서 보내기
  public void sendAccessAndRefreshToken(HttpServletResponse response, String accessToken,
      String refreshToken) {
    response.setStatus(HttpServletResponse.SC_OK);
    setAccessTokenHeader(response, accessToken);
    setRefreshTokenHeader(response, refreshToken);
    log.info("Access Token 및 Refresh Token 헤더 설정 완료");
  }

  // 헤더에서 AccessToken 추출
  public Optional<String> extractAccessToken(HttpServletRequest request) {
    return Optional.ofNullable(request.getHeader(accessHeader))
        .filter(refreshToken -> refreshToken.startsWith(TOKEN_PREFIX))
        .map(refreshToken -> refreshToken.replace(TOKEN_PREFIX, ""));
  }

  // 헤더에서 RefreshToken 추출
  public Optional<String> extractRefreshToken(HttpServletRequest request) {
    return Optional.ofNullable(request.getHeader(refreshHeader))
        .filter(refreshToken -> refreshToken.startsWith(TOKEN_PREFIX))
        .map(refreshToken -> refreshToken.replace(TOKEN_PREFIX, ""));
  }

  // RefreshToken DB 저장 메서드
  @Transactional
  public void updateRefreshToken(String email, String refreshToken) {
    memberRepository.findByEmail(email)
        .ifPresentOrElse(
            member -> member.updateRefreshToken(refreshToken),
            () -> new CustomException(ErrorCode.MEMBER_NOT_FOUND)
        );
  }

  // RefreshToken 재발급 및 DB 업데이트 메서드
  public String recreateRefreshToken(Member member) {
    String newRefreshToken = createRefreshToken();
    member.updateRefreshToken(newRefreshToken);
    memberRepository.saveAndFlush(member);
    return newRefreshToken;
  }

  // AccessToken에서 memberId 추출
  public Long extractMemberId(String accessToken) {
    Claims claims = extractClaims(accessToken);
    return claims.get("id", Long.class);
  }

  // AccessToken에서 email 추출
  public String extractEmail(String accessToken) {
    Claims claims = extractClaims(accessToken);
    return claims.get("email", String.class);
  }

  // AccessToken에서 nickname 추출
  public String extractNickname(String accessToken) {
    Claims claims = extractClaims(accessToken);
    return claims.get("nickname", String.class);
  }

  // AccessToken에서 Claim 추출 : id, email, nickname
  private Claims extractClaims(String accessToken) {
    return Jwts.parser()
        .setSigningKey(secretKey)
        .parseClaimsJws(accessToken)
        .getBody();
  }

  // AccessToken 헤더 설정
  private void setAccessTokenHeader(HttpServletResponse response, String accessToken) {
    response.setHeader(accessHeader, accessToken);
  }

  // RefreshToken 헤더 설정
  private void setRefreshTokenHeader(HttpServletResponse response, String refreshToken) {
    response.setHeader(refreshHeader, refreshToken);
  }

}
