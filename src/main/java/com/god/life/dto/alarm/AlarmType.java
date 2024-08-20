package com.god.life.dto.alarm;

import com.god.life.domain.CategoryType;

import java.util.Arrays;

public enum AlarmType {

    ENTIRE("all"),
    TODO("todo"),
    COMMENT("comment"),
    STIMULUS("stimulus"),
    NORMAL("normal");

    private final String alarmType;

    AlarmType(String type) {
        this.alarmType = type;
    }

    public String getType() {
        return alarmType;
    }

    public static AlarmType toAlarmType(CategoryType categoryType){
        String boardType = categoryType.getType();
        return Arrays.stream(values()).filter(type -> type.alarmType.equals(boardType)).findAny().get();
    }

}
