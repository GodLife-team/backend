package com.god.life.dto.fcm;

import lombok.*;

/**
 * 핸드폰에서 보낼 메세지
 */

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class FcmSendDto {
    
    private String token;
    private String title;
    private String body;


    
}
