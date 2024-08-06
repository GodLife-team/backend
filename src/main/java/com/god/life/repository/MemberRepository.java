package com.god.life.repository;

import com.god.life.domain.Member;
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

    @Query("SELECT m, sum(b.totalScore) from Member m left join Board b on m.id = b.member.id where m.id = :memberId")
    Optional<Member> findByIdWithImage(@Param("memberId") Long memberId);

    Optional<Member> findByProviderId(String providerId);

    @Modifying
    @Query("update Member m set m.fcmToken = :fcm where m.id = :memberId")
    void updateFcm(@Param("fcm") String fcm, @Param("memberId") Long memberId);

    @Query("select m.fcmToken from Member m where m.fcmToken is not null")
    Optional<List<String>> findAllFcmToken();

    @Modifying
    @Query("update Member m set m.checkAlarm = :onOff where m.id = :memberId")
    void updateAlarmOption(@Param("memberId") Long memberId, @Param("onOff") boolean onOff);

}
