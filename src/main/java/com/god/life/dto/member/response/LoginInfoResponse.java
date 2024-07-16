package com.god.life.dto.member.response;

import com.god.life.domain.Member;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginInfoResponse {

    @Schema(description = "닉네임")
    private String nickname;

    @Schema(description = "나이")
    private int age;

    @Schema(description = "성별")
    private String sex;

    @Schema(description = "갓생 점수")
    private int godLifeScore;

    @Schema(description = "프로필 사진 URL")
    private String profileImage;

    @Schema(description = "배경화면 사진 URL")
    private String backgroundImage;

    @Schema(description = "자기 소개")
    private String whoAmI;

    @Schema(description = "해당 회원 memberId")
    private Long memberId;

    @Schema
    private String fcm;

    public static LoginInfoResponse from(Member findMember) {
        return LoginInfoResponse.builder()
                .age(findMember.getAge())
                .sex(findMember.getSex().getSex())
                .nickname(findMember.getNickname())
                .godLifeScore((int) findMember.getGodLifePoint())
                .backgroundImage(findMember.getBackgroundName() == null ? "" : findMember.getBackgroundName())
                .whoAmI(findMember.getWhoAmI())
                .memberId(findMember.getId())
                .fcm(findMember.getFcmToken())
                .profileImage(findMember.getProfileName() == null ? "" : findMember.getProfileName()).build();
    }
}
