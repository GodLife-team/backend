package com.god.life.dto;


import com.god.life.domain.Member;
import com.god.life.domain.ProviderType;
import com.god.life.domain.Sex;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.sound.midi.MetaMessage;

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

    @NotBlank(message = "유저 식별 번호는 반드시 있어야 합니다.")
    private String providerId;

    @NotBlank(message = "제공자 회사 명은 반드시 있어야 합니다.")
    private String providerName;


    public static Member toMember(SignupRequest request){
        return Member.builder()
                .age(request.getAge())
                .sex(Sex.findSex(request.getSex()))
                .email(request.getEmail())
                .godLifePoint(0L)
                .providerName(ProviderType.KAKAO)
                .providerId(request.getProviderId())
                .whoAmI("") //가입할땐 빈 문자열로 전달
                .nickname(request.getNickname()).build();
    }


}
