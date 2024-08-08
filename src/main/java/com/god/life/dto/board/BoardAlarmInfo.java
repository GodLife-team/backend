package com.god.life.dto.board;

import com.god.life.domain.CategoryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder
public class BoardAlarmInfo {

    private String title;
    private String content = "";
    private Long boardId;
    private CategoryType categoryType;


}
