package com.god.life.repository;

import com.god.life.domain.FcmAlarm;
import com.god.life.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FcmAlarmRepository extends JpaRepository<FcmAlarm, String> {

    //지금 보내야 하는 토큰 반환
    @Query("select m.fcmToken from FcmAlarm f join f.member m where f.sendTime = :now")
    List<String> findSendUserTokens(@Param(value = "now") LocalDateTime now);


    // 금일 생성된 알람을 삭제한다
    @Modifying
    @Query("delete FcmAlarm f where f.member = :member and function('DATE', f.sendTime) = " +
            "function('DATE', now())")
    void deleteTodayAlarm(@Param("member") Member member);

    // 금일 알람 가져옴
    @Query("select f from FcmAlarm f where f.member = :member and f.sendTime = :now")
    Optional<FcmAlarm> selectTodayAlarm(@Param("member") Member member);

}
