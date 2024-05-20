package com.god.life.domain;

import java.util.Arrays;

public enum Sex {
    MALE("남"), FEMALE("여");

    private final String sex;

    Sex(String sex) {
        this.sex = sex;
    }

    public String getSex(){
        return this.sex;
    }

    static public Sex findSex(String sex) {
        return Arrays.stream(Sex.values()).filter(s -> s.sex.equals(sex)).findFirst().orElse(MALE);
    }
}
