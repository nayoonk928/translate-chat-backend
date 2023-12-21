package com.nayoon.translatechat.config.oauth.handler;

import com.nayoon.translatechat.config.jwt.JwtService;
import com.nayoon.translatechat.config.oauth.userinfo.OAuth2UserInfo;
import com.nayoon.translatechat.config.oauth.userinfo.OAuth2UserInfoFactory;
import com.nayoon.translatechat.entity.Member;
import com.nayoon.translatechat.exception.CustomException;
import com.nayoon.translatechat.exception.ErrorCode;
import com.nayoon.translatechat.repository.MemberRepository;
import com.nayoon.translatechat.type.SocialType;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

  private final JwtService jwtService;
  private final MemberRepository memberRepository;

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) throws IOException, ServletException {

    try {
      OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
      SocialType socialType =
          SocialType.valueOf(token.getAuthorizedClientRegistrationId().toUpperCase());

      OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
      OAuth2UserInfo userInfo =
          OAuth2UserInfoFactory.getOAuth2UserInfo(socialType, oAuth2User.getAttributes());

      Member member = memberRepository.findByEmail(userInfo.getEmail())
          .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

      // 액세스 토큰 생성, 리프레시 토큰 생성
      String accessToken = jwtService.createAccessToken(member);
      String refreshToken = jwtService.createRefreshToken();

      response.addHeader(jwtService.getAccessHeader(), "Bearer " + accessToken);
      response.addHeader(jwtService.getRefreshHeader(), "Bearer " + refreshToken);

      jwtService.sendAccessAndRefreshToken(response, accessToken, refreshToken);
      jwtService.updateRefreshToken(userInfo.getEmail(), refreshToken);
    } catch (Exception e) {
      log.error("OAuth2 인증 성공 처리 중 오류 발생. 에러 메시지 : {}", e.getMessage());
      throw new ServletException("OAuth2 인증 성공 처리에 실패했습니다. 에러 메시지 : {}", e);
    }
  }

}
