package com.god.life.dto.member.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignupResponse {

    private String accessToken;
    private String refreshToken;

}
