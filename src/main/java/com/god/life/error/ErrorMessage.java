package com.god.life.error;

public enum ErrorMessage {

    INVALID_BOARD_MESSAGE("존재하지 않는 게시판 입니다."),
    INVALID_MEMBER_MESSAGE("존재하지 않는 회원입니다."),
    INVALID_COMMENT_MESSAGE("존재하지 않는 댓글입니다."),
    FORBIDDEN_ACTION_MESSAGE("해당 작업을 수행할 권한이 없습니다."),

    ALREADY_MARKED_MESSAGE("이미 갓생 인정한 게시물 입니다");

    private final String errorMessage;

    ErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
