package com.god.life.domain;

import java.util.Arrays;

public enum Sex {
    MALE, FEMALE;


    static public Sex findSex(String sex) {
        return Arrays.stream(Sex.values()).filter(s -> s.name().equals(sex)).findFirst().orElse(FEMALE);
    }
}
