package com.god.life.service;

import com.god.life.domain.FcmAlarm;
import com.god.life.domain.Member;
import com.god.life.dto.AlarmCreateRequest;
import com.god.life.dto.fcm.FcmSendDto;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class FcmAlarmService {

    private final FcmAlarmRepository fcmAlarmRepository;
    private final FirebaseMessaging messenger;


    @Transactional(readOnly = true)
    public List<String> getUserTokenAtTime(LocalDateTime time) {
        return fcmAlarmRepository.findSendUserTokens(time);
    }

    // 금일 알람을 생성합니다.
    @Transactional
    public void createTodayAlarm(AlarmCreateRequest request, Member member) {
        LocalDateTime sendTime = request.toTime();
        // 보내야 하는 시간이 이미 지나간 시간이면
        if (sendTime.isBefore(LocalDateTime.now())) {
            throw new BadRequestException("현재 시간보다 이전의 알람을 등록할 수 없습니다");
        }
        FcmAlarm fcmAlarm = FcmAlarm.builder()
                .member(member)
                .sendTime(sendTime).build();
        fcmAlarmRepository.save(fcmAlarm);
    }

    /**
     * sendDTO에 token값에 대응되는 기기에 DTO 값 전송
     */
    public String sendNotification(FcmSendDto fcmSendDto) {
        String sendToUser = fcmSendDto.getToken();

        Notification notification = Notification.builder()
                .setTitle(fcmSendDto.getTitle())
                .setBody(fcmSendDto.getBody())
                .build();

        MulticastMessage multicastMessage =
                MulticastMessage.builder()
                        .setNotification(notification)
                        .addAllTokens(List.of(fcmSendDto.getToken()))
                        .build();
        try{
            messenger.sendEachForMulticast(multicastMessage);
            return "TokenValue : " + sendToUser + "fcm 메세지 전송 성공!";
        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
            return "메세지 전송 실패.. 로그 확인";
        }
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
        // 보내야 하는 시간이 이미 지나간 시간이면
        if (sendTime.isBefore(LocalDateTime.now())) {
            throw new BadRequestException("현재 시간보다 이전의 알람을 등록할 수 없습니다");
        }

        FcmAlarm fcmAlarm = fcmAlarmRepository.selectTodayAlarm(member).orElseThrow(() ->
                new BadRequestException("잘못된 알람 업데이트 요청입니다."));

        //변경 감지
        fcmAlarm.updateAlarm(sendTime);
    }

    private static final String END_MESSAGE_TITLE = "오늘 TODO 까먹지 않으셨죠?";
    private static final String END_MESSAGE_BODY = "오늘 굿생기록을 정리해 보세요!";


    // 1분마다
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
