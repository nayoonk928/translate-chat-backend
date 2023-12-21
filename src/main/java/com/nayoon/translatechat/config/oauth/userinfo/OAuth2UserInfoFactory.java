package com.nayoon.translatechat.config.oauth.userinfo;

import com.nayoon.translatechat.config.oauth.userinfo.impl.GoogleOAuth2UserInfo;
import com.nayoon.translatechat.config.oauth.userinfo.impl.KakaoOAuth2UserInfo;
import com.nayoon.translatechat.config.oauth.userinfo.impl.NaverOAuth2UserInfo;
import com.nayoon.translatechat.exception.CustomException;
import com.nayoon.translatechat.exception.ErrorCode;
import com.nayoon.translatechat.type.SocialType;
import java.util.Map;

public class OAuth2UserInfoFactory {

  public static OAuth2UserInfo getOAuth2UserInfo(SocialType socialType, Map<String, Object> attributes) {
    switch (socialType) {
      case GOOGLE: return new GoogleOAuth2UserInfo(attributes);
      case NAVER: return new NaverOAuth2UserInfo(attributes);
      case KAKAO: return new KakaoOAuth2UserInfo(attributes);
      default: throw new CustomException(ErrorCode.INVALID_SOCIAL_TYPE);
    }
  }

}
