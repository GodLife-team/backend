package com.god.life.service.alarm;

import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class GoogleAlarmSender implements AlarmSender {

    private final FirebaseMessaging sender;
    private static final String MESSAGE_DATA_KEY = "boardId";


    @Override
    public void sendAlarm(Long boardId, String token, String title, String content) {
        Notification notification = makeNotification(title, content);
        Message message = Message.builder()
                //.setNotification(notification)
                .putData("title" , title)
                .putData("content", content)
                .putData(MESSAGE_DATA_KEY, String.valueOf(boardId))
                .setToken(token)
                .build();

        try {
            sender.send(message);
        } catch (FirebaseMessagingException e) {
            log.error("메세지 전송 실패... token = {}, 제목 = {}, 내용 = {}", token, title, content);
        }

    }

    // 매 분마다 알람을 보낼 유저의 토큰값을 얻어내 해당 유저의 기기로 알림을 전송합니다.
    @Override
    public void sendAlarm(List<String> tokens, String title, String content) {
        //보낼 대상이 없으면 알림 전송 XXX
        if (tokens == null || tokens.size() == 0) {
            return;
        }

        log.info("이번에 보낼 사람 : {}", tokens);

        Notification notification = makeNotification(title, content);
        MulticastMessage message = MulticastMessage
                .builder()
                //.setNotification(notification)
                .putData("title" , title)
                .putData("content", content)
                .putData(MESSAGE_DATA_KEY, null) //공통 처리를 위한 null 전송
                .addAllTokens(tokens)
                .build();


        try {
            BatchResponse batchResponse = sender.sendEachForMulticast(message);
            int successCount = batchResponse.getSuccessCount();
            if (successCount != tokens.size()) {
                log.error("FCM 전송 {} 명에게 전송 실패!", tokens.size() - successCount);
                List<SendResponse> responses = batchResponse.getResponses();
                List<String> failTokens = new ArrayList<>(); //실패한 유저 ID LOG 저장
                for (int i = 0; i < responses.size(); i++) { //실패한 애들 찾아서 저장
                    SendResponse response = responses.get(i);
                    if (!response.isSuccessful()) {
                        failTokens.add(tokens.get(i));
                    }
                }
                log.error("전송 실패.. {}", failTokens);
            }
            log.info("알람 전송 성공!!!");
        } catch (FirebaseMessagingException e) {
            log.error("FCM 전송 오류!", e);
        }
    }

    private Notification makeNotification(String title, String content) {
        return Notification
                .builder()
                .setTitle(title)
                .setBody(content)
                .build();
    }


}
