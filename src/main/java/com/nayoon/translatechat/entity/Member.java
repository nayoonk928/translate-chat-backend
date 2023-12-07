package com.nayoon.translatechat.entity;


import com.nayoon.translatechat.type.MemberStatus;
import com.nayoon.translatechat.type.Provider;
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

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

  @Id
  @Column(name = "member_id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  @Column(nullable = false, unique = true, name = "nickname")
  String nickname;

  @Column(nullable = false, name = "email")
  String email;

  @Column(nullable = false, name = "social_id")
  String socialId;

  @Column(nullable = false, name = "member_status")
  @Enumerated(EnumType.STRING)
  MemberStatus status;

  @Column(nullable = false, name = "provider")
  @Enumerated(EnumType.STRING)
  Provider provider;

  @CreatedDate
  @Column(nullable = false, name = "created_at")
  LocalDateTime createdAt;

  @CreatedDate
  @Column(nullable = false, name = "modified_at")
  LocalDateTime modifiedAt;

  @Builder
  public Member(String nickname, String email, String socialId, MemberStatus status,
      Provider provider, LocalDateTime createdAt, LocalDateTime modifiedAt) {
    this.nickname = nickname;
    this.email = email;
    this.socialId = socialId;
    this.status = status;
    this.provider = provider;
    this.createdAt = createdAt;
    this.modifiedAt = modifiedAt;
  }

  // for test
  @Builder
  public Member(Long id, String nickname, String email, String socialId, MemberStatus status,
      Provider provider, LocalDateTime createdAt, LocalDateTime modifiedAt) {
    this.id = id;
    this.nickname = nickname;
    this.email = email;
    this.socialId = socialId;
    this.status = status;
    this.provider = provider;
    this.createdAt = createdAt;
    this.modifiedAt = modifiedAt;
  }

}
