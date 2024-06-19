package com.god.life.dto.fcm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * FCM에 전송될 메세지 데이터
 */

@Getter
@Builder
public class FcmMessageDto {

    private boolean validateOnly;
    private FcmMessageDto.Message message;


    @Builder
    @AllArgsConstructor
    @Getter
    public static class Message {
        private FcmMessageDto.Notification notification;
        private String token;
    }

    @Builder
    @AllArgsConstructor
    @Getter
    public static class Notification {
        private String title;
        private String body;
        private String image;
    }

}
