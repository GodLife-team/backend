package com.god.life.repository;

import com.god.life.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {


    Optional<Member> findByRefreshToken(String refreshToken);

    boolean existsByNickname(String nickname);

    boolean existsByEmail(String email);
}
