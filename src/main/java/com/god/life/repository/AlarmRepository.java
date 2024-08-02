package com.god.life.repository;

import com.god.life.domain.Alarm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AlarmRepository extends JpaRepository<Alarm, Long> {

    //읽지 않은 알람을 가져옵니다.
    @Query("select a from Alarm a where a.memberId = :memberId and a.isRead = false")
    Optional<List<Alarm>> findUnreadAlarmByMemberId(@Param("memberId") Long loginMemberId);
    
}
