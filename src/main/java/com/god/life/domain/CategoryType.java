package com.god.life.domain;

public enum CategoryType {

    GOD_LIFE_PAGE("normal"),
    GOD_LIFE_STIMULUS("stimulus");

    private String type;

    CategoryType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
