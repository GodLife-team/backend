package com.god.life.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PopularBoardQueryDTO {

    private Long boardId; // 게시판
    private int sum; // 갓생 점수

}
