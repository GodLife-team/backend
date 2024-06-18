package com.god.life.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PopularMemberResponse {

    @Schema(description = "해당 회원 MEMBER_ID")
    private Long memberId;

    @Schema(description = "닉네임")
    private String nickname;

    @Schema(description = "해당 회원이 해당 기간동안 받은 갓생 점수")
    private int godLifeScore;

    @Schema(description = "자기소개")
    private String whoAmI;

    @Schema(description = "프로필 이미지 URL")
    private String profileURL = "";

}
