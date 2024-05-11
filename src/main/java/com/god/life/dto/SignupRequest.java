package com.god.life.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignupRequest {

    @NotBlank
    @Length(max = 10, message = "닉네임은 최대 10자입니다.")
    private String nickname;

    @NotBlank(message = "이메일은 반드시 존재해야 합니다.")
    private String email;

    private Integer age;

    private String sex;

    @NotBlank(message = "토큰 값은 반드시 있어야 합니다.")
    private String providerToken;

    @NotBlank(message = "제공자 회사 명은 반드시 있어야 합니다.")
    private String providerName;
}
