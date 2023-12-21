package com.nayoon.translatechat.entity;

import com.nayoon.translatechat.exception.CustomException;
import com.nayoon.translatechat.exception.ErrorCode;
import com.nayoon.translatechat.type.MemberStatus;
import com.nayoon.translatechat.type.SocialType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

  @Id
  @Column(name = "member_id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, name = "nickname")
  private String nickname;

  @Column(nullable = false, unique = true, name = "email")
  private String email;

  @Column(nullable = false, name = "image_url")
  private String imageUrl;

  @Column(nullable = false, name = "social_id")
  private String socialId;

  @Column(nullable = false, name = "status")
  @Enumerated(EnumType.STRING)
  private MemberStatus status;

  @Column(nullable = false, name = "social_type")
  @Enumerated(EnumType.STRING)
  private SocialType socialType;

  private String refreshToken;

  @CreatedDate
  @Column(nullable = false, name = "created_at")
  private LocalDateTime createdAt;

  @LastModifiedDate
  @Column(nullable = false, name = "modified_at")
  private LocalDateTime modifiedAt;

  public void updateRefreshToken(String refreshToken) {
    if (refreshToken == null) {
      throw new CustomException(ErrorCode.REFRESH_TOKEN_NOT_CREATED);
    }
    this.refreshToken = refreshToken;
  }

  @Builder
  public Member(String nickname, String email, String imageUrl, String socialId, MemberStatus status,
      SocialType socialType, String refreshToken, LocalDateTime createdAt, LocalDateTime modifiedAt) {
    this.nickname = nickname;
    this.email = email;
    this.imageUrl = imageUrl;
    this.socialId = socialId;
    this.status = status;
    this.socialType = socialType;
    this.refreshToken = refreshToken != null ? refreshToken : this.refreshToken;
    this.createdAt = createdAt;
    this.modifiedAt = modifiedAt;
  }

  // for test
  @Builder
  public Member(Long id, String nickname, String email, String imageUrl, String socialId,
      MemberStatus status, SocialType socialType, String refreshToken, LocalDateTime createdAt,
      LocalDateTime modifiedAt) {
    this.id = id;
    this.nickname = nickname;
    this.email = email;
    this.imageUrl = imageUrl;
    this.socialId = socialId;
    this.status = status;
    this.socialType = socialType;
    this.refreshToken = refreshToken;
    this.createdAt = createdAt;
    this.modifiedAt = modifiedAt;
  }

}
