package com.god.life.dto;

import com.god.life.domain.Sex;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginInfoResponse {

    private String nickname;
    private int age;
    private String sex;
    private int godLifeScore;
    private String profileImage;
    private String backgroundImage;

}
