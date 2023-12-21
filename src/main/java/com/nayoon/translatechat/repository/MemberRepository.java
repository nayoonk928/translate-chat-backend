package com.nayoon.translatechat.repository;

import com.nayoon.translatechat.entity.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

  Optional<Member> findByEmail(String email);
  Optional<Member> findByRefreshToken(String refreshToken);

}
