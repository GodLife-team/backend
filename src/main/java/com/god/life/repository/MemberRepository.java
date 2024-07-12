package com.god.life.repository;

import com.god.life.domain.Member;
import com.god.life.dto.MemberInfoResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long>, CustomMemberRepository {

    boolean existsByNickname(String nickname);

    boolean existsByEmail(String email);

    boolean existsByProviderId(String id);

    @Query("SELECT m from Member m left join fetch m.images where m.id = :memberId")
    Optional<Member> findByIdWithImage(@Param("memberId") Long memberId);

    Optional<Member> findByProviderId(String providerId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Member m set m.godLifePoint = m.godLifePoint + 2 where m.id = :memberId")
    void incrementGodLifeScore(@Param("memberId") Long memberId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Member m set m.godLifePoint = m.godLifePoint - 2 where m.id = :memberId")
    void decrementGodLifeScore(@Param("memberId") Long memberId);
}
