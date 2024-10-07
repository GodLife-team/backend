package com.god.life.dto.recommend.response;

import com.god.life.domain.Member;
import com.god.life.dto.board.response.GodLifeStimulationBoardBriefResponse;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
public class RecommendAuthorResponse {

    private String nickname;
    private String profileUrl;
    private String backgroundUrl;
    private String whoAmI;
    private List<GodLifeStimulationBoardBriefResponse> responses;

    public RecommendAuthorResponse() {
        this.nickname = "";
        this.profileUrl = "";
        this.backgroundUrl = "";
        this.whoAmI = "";
        this.responses = new ArrayList<>();
    }

    public RecommendAuthorResponse(Member member, List<GodLifeStimulationBoardBriefResponse> responses) {
        nickname = member.getNickname();
        profileUrl = member.getProfileName();
        backgroundUrl = member.getBackgroundName();
        whoAmI = member.getWhoAmI();
        this.responses = responses;
    }


}
