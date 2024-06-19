package com.god.life.service.scheduler;


import com.god.life.service.FcmAlarmService;
import com.google.firebase.messaging.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class FcmNotificationScheduler {

    private final FcmAlarmService fcmAlarmService;

    private final FirebaseMessaging messenger;

    public FcmNotificationScheduler(FcmAlarmService fcmAlarmService,
                                  FirebaseMessaging firebaseMessaging) {
        this.fcmAlarmService = fcmAlarmService;
        this.messenger = firebaseMessaging;
    }

    private static final String END_MESSAGE_TITLE = "오늘 하루도 고생하셨어요!";
    private static final String END_MESSAGE_BODY = "오늘 하루를 정리해 보세요!";


    // 1분마다
    @Scheduled(cron = "0 * * * * *")
    public void sendMessage() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        List<String> sendToUserTokens = fcmAlarmService.getUserTokenAtTime(now);

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
