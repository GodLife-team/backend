package com.god.life.service.alarm;

import java.util.List;

public interface AlarmSender {


    // token 값을 대해 title, content로 메세지를 보낸다.
    void sendAlarm(Long boardId, String token, String title, String content);

    // 갓생 기록 알람 시간에 대해 전달.
    void sendAlarm(List<String> token, String title, String content);




}
