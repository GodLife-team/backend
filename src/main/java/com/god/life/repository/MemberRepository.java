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

    Optional<Member> findByProviderId(String providerId);

}
