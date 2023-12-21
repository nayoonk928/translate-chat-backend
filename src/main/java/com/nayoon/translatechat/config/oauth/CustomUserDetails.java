package com.nayoon.translatechat.config.oauth;

import com.nayoon.translatechat.entity.Member;
import java.util.Collection;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

@Getter
@Setter
public class CustomUserDetails implements OAuth2User, UserDetails {

  private Member member;
  private Map<String, Object> attributes;

  public CustomUserDetails(Member member) {
    this.member = member;
  }

  public static CustomUserDetails create(Member member) {
    return new CustomUserDetails(member);
  }

  public static CustomUserDetails create(Member member, Map<String, Object> attributes) {
    CustomUserDetails customUserDetails = create(member);
    customUserDetails.setAttributes(attributes);
    return customUserDetails;
  }

  @Override
  public String getPassword() {
    return null;
  }

  @Override
  public String getUsername() {
    return member.getEmail();
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  @Override
  public Map<String, Object> getAttributes() {
    return attributes;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return null;
  }

  @Override
  public String getName() {
    return member.getNickname();
  }

}
