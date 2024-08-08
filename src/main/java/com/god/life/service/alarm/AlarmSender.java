package com.god.life.service.alarm;

import com.god.life.domain.CategoryType;
import com.god.life.dto.board.BoardAlarmInfo;

import java.util.List;

public interface AlarmSender {


    // token 값을 대해 title, content로 메세지를 보낸다.
    void sendAlarm(String token, BoardAlarmInfo info);

    // 갓생 기록 알람 시간에 대해 전달.
    void sendAlarm(List<String> token, String title, String content);




}
