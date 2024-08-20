package com.god.life.service.alarm;

import com.god.life.domain.Board;
import com.god.life.domain.FcmAlarm;
import com.god.life.domain.Member;
import com.god.life.dto.alarm.AlarmType;
import com.god.life.dto.alarm.request.AlarmCreateRequest;
import com.god.life.error.BadRequestException;
import com.god.life.repository.BoardRepository;
import com.god.life.repository.FcmAlarmRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;

/**
 * Firebase의 Messging 을 통해 매 분마다 알림을 보내는 클래스입니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FcmAlarmService {

    private final FcmAlarmRepository fcmAlarmRepository;
    private final AlarmSender alarmSender;
    private static final String END_MESSAGE_TITLE = "오늘 TODO 까먹지 않으셨죠?";
    private static final String END_MESSAGE_BODY = "오늘 굿생기록을 정리해 보세요!";

    public List<String> getUserTokenAtTime(LocalDateTime time) {
        return fcmAlarmRepository.findSendUserTokens(time);
    }

    // 금일 알람을 생성합니다.
    @Transactional
    public void createTodayAlarm(AlarmCreateRequest request, Member member) {
        LocalDateTime sendTime = request.toTime();
        FcmAlarm fcmAlarm = new FcmAlarm(member, sendTime);
        fcmAlarmRepository.save(fcmAlarm);
    }

    //금일 생성된 알람을 제거합니다
    @Transactional
    public void deleteTodayAlarm(Member member) {
        fcmAlarmRepository.deleteTodayAlarm(member);
    }

    // 금일 생성된 알람 시간을 바꿉니다.
    @Transactional
    public void updateTodayAlarm(AlarmCreateRequest alarmCreateRequest, Member member) {
        LocalDateTime sendTime = alarmCreateRequest.toTime();

        FcmAlarm fcmAlarm = fcmAlarmRepository.selectTodayAlarm(member).orElseThrow(() ->
                new BadRequestException("잘못된 알람 업데이트 요청입니다."));

        //변경 감지
        fcmAlarm.updateAlarm(sendTime);
    }

    // 매 분마다 알람을 보낼 유저의 토큰값을 얻어내 해당 유저의 기기로 알림을 전송합니다.
    @Scheduled(cron = "0 * * * * *")
    public void sendMessage() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        List<String> sendToUserTokens = getUserTokenAtTime(now);
        alarmSender.sendAlarm(sendToUserTokens, END_MESSAGE_TITLE, END_MESSAGE_BODY, AlarmType.TODO);
    }

}
