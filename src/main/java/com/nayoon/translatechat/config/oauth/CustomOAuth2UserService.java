package com.nayoon.translatechat.config.oauth;

import com.nayoon.translatechat.config.oauth.userinfo.OAuth2UserInfo;
import com.nayoon.translatechat.config.oauth.userinfo.OAuth2UserInfoFactory;
import com.nayoon.translatechat.entity.Member;
import com.nayoon.translatechat.exception.CustomException;
import com.nayoon.translatechat.exception.ErrorCode;
import com.nayoon.translatechat.repository.MemberRepository;
import com.nayoon.translatechat.type.MemberStatus;
import com.nayoon.translatechat.type.SocialType;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

  private final MemberRepository memberRepository;

  @Override
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
    OAuth2User user = delegate.loadUser(userRequest);

    return saveOrUpdate(userRequest, user);
  }

  private OAuth2User saveOrUpdate(OAuth2UserRequest userRequest, OAuth2User user) {
    // Provider : GOOGLE, NAVER, KAKAO
    SocialType socialType = SocialType.valueOf(
        userRequest.getClientRegistration().getRegistrationId().toUpperCase());
    OAuth2UserInfo userInfo =
        OAuth2UserInfoFactory.getOAuth2UserInfo(socialType, user.getAttributes());

    Member member = memberRepository.findByEmail(userInfo.getEmail()).orElse(null);

    // 해당 이메일로 가입한 회원이 있는 경우
    if (member != null) {
      if (socialType != member.getSocialType()) {
        throw new CustomException(ErrorCode.SOCIAL_TYPE_MISS_MATCH);
      }
    } else {
      // 해당 이메일로 가입한 회원이 없는 경우 새로 생성
      member = createMember(userInfo, socialType);
    }

    return CustomUserDetails.create(member, user.getAttributes());
  }

  private Member createMember(OAuth2UserInfo userInfo, SocialType socialType) {
    LocalDateTime now = LocalDateTime.now();
    Member member = Member.builder()
        .nickname(userInfo.getName())
        .email(userInfo.getEmail())
        .imageUrl(userInfo.getImageUrl())
        .socialId(userInfo.getId())
        .status(MemberStatus.ACTIVE)
        .socialType(socialType)
        .createdAt(now)
        .modifiedAt(now)
        .build();
    memberRepository.saveAndFlush(member);

    return member;
  }

}
