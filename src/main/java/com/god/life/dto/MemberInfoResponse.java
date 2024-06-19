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
public class MemberInfoResponse {

    @Schema(description = "닉네임")
    private String nickname;

    @Schema(description = "자기소개")
    private String whoAmI;

    @Schema(description = "프로필 이미지 URL")
    private String profileImageURL;

    @Schema(description = "배경화면 이미지 URL")
    private String backgroundImageURL;

    @Schema(description = "해당 회원이 받은 갓생 점수")
    private int godLifeScore;

    @Schema(description = "해당 회원이 작성한 게시물 수")
    private Long memberBoardCount;

    @Schema(description = "자기 정보 조회인지 true/false")
    private boolean isOwner;


    public MemberInfoResponse(String nickname, String whoAmI, Long memberBoardCount) {
        this.nickname = nickname;
        this.whoAmI = whoAmI;
        this.memberBoardCount = memberBoardCount;
        this.profileImageURL = "";
        this.backgroundImageURL = "";
    }


}
