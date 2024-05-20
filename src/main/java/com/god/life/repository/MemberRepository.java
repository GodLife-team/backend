package com.god.life.repository;

import com.god.life.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {


    Optional<Member> findByRefreshToken(String refreshToken);

    boolean existsByNickname(String nickname);

    boolean existsByEmail(String email);

    boolean existsByProviderId(String id);

    @Query("SELECT m from Member m join fetch m.images where m.id = :memberId")
    Member findByIdWithImage(@Param("memberId") Long memberId);
}
