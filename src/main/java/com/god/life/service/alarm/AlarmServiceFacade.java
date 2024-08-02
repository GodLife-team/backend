package com.god.life.service.alarm;

import com.god.life.domain.Alarm;
import com.god.life.dto.alarm.response.AlarmResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlarmServiceFacade {

    private final AlarmService alarmService;
    private final AlarmSender alarmSender;

    // 알람 생성 후 외부 API에 알람을 보내야 할지 결정
    // 알람 생성하는 곳에서 외부 API를 호출하면 외부 API가 오래 걸리면 커넥션 낭비 --> 트랜잭션에서 분리
    public void processAlarm(Long boardId, Long loginMemberId, String alarmTitle, String alarmBody) {
        log.info("이번에 보낼 알람 제목 = {}, 내용 = {}", alarmTitle, alarmBody);

        // 1. 알람 저장
        String token = alarmService.saveAlarm(boardId, loginMemberId, alarmTitle, alarmBody);

        log.info("이번에 보낼 FCM 토큰 값 = {}", token);
        // 2. 알람 전송 ==> 이 때 본인이 작성한 게시물의 댓글은 알림을 보내지 않는다.
        if (token != null) {
            alarmSender.sendAlarm(boardId, token, alarmTitle, alarmBody);
        }

    }


    // 현재 로그인한 유저의 알람을 가져온다.
    public List<AlarmResponse> getUnreadAlarm(Long loginMemberId){
        List<Alarm> unreadAlarm = alarmService.getUnreadAlarm(loginMemberId);
        return unreadAlarm.stream().map(AlarmResponse::of).toList();
    }

}
