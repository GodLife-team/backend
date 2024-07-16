package com.god.life.service;

import com.god.life.domain.FcmAlarm;
import com.god.life.domain.Member;
import com.god.life.dto.alarm.request.AlarmCreateRequest;
import com.god.life.error.BadRequestException;
import com.god.life.repository.FcmAlarmRepository;
import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Firebase의 Messging 을 통해 매 분마다 알림을 보내는 클래스입니다.
 */

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class FcmAlarmService {

    private final FcmAlarmRepository fcmAlarmRepository;
    private final FirebaseMessaging messenger;

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

        //보낼 대상이 없으면 알림 전송 XXX
        if(sendToUserTokens == null || sendToUserTokens.size() == 0){
            return;
        }

        log.info("이번에 보낼 사람 : {}", sendToUserTokens);

        Notification notification = Notification
                .builder()
                .setTitle(END_MESSAGE_TITLE)
                .setBody(END_MESSAGE_BODY)
                .build();
        MulticastMessage message = MulticastMessage
                .builder()
                .setNotification(notification)
                .addAllTokens(sendToUserTokens)
                .build();

        try{
            BatchResponse batchResponse = messenger.sendEachForMulticast(message);
            int successCount = batchResponse.getSuccessCount();
            if (successCount != sendToUserTokens.size()) {
                log.error("FCM 전송 {} 명에게 전송 실패!", sendToUserTokens.size() - successCount);
                List<SendResponse> responses = batchResponse.getResponses();
                List<String> failTokens = new ArrayList<>(); //실패한 유저 ID LOG 저장
                for (int i=0; i<responses.size(); i++) { //실패한 애들 찾아서 저장
                    SendResponse response = responses.get(i);
                    if(!response.isSuccessful()){
                        failTokens.add(sendToUserTokens.get(i));
                    }
                }
                log.error("전송 실패.. {}", failTokens);
            }
            log.info("알람 전송 성공!!!");
        } catch (FirebaseMessagingException e) {
            log.error("FCM 전송 오류!", e);
        }
    }

}
