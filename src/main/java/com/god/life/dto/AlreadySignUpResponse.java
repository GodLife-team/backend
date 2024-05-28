package com.god.life.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlreadySignUpResponse extends TokenResponse {

    private String alreadySignUp;

    public AlreadySignUpResponse(String accessToken, String refreshToken, String alreadySignUp) {
        super(accessToken, refreshToken);
        this.alreadySignUp = alreadySignUp;
    }

    public void updateResponse(TokenResponse token, String check) {
        alreadySignUp = check;
        accessToken = token.getAccessToken();
        refreshToken = token.getRefreshToken();
    }
}
