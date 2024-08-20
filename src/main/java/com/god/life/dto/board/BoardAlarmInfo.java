package com.god.life.dto.board;

import com.god.life.domain.CategoryType;
import com.god.life.dto.alarm.AlarmType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@Builder
@ToString
public class BoardAlarmInfo {

    private String title;
    private String content = "";
    private Long boardId;
    private AlarmType alarmType;
    private CategoryType boardCategory;



}
