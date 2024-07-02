package com.god.life.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;


@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
public class GodStimulationBoardSearchRequest {


    @Schema(description = "제목")
    String title;

    @Schema(description = "닉네임")
    String nickname;

    @Schema(description = "소개글")
    String introduction;


}
