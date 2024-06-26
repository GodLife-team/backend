package com.god.life.domain;

public enum BoardStatus {

    T("temporary"), S("save");

    final String status;

    BoardStatus(String status) {
        this.status = status;
    }
}
