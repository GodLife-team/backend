package com.god.life.dto;

import com.god.life.domain.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BoardCreateRequest {

    @Schema(description = "제목")
    private String title;

    @Schema(description = "본문")
    private String content;

    @Schema(description = "태그")
    private List<String> tags = new ArrayList<>();

    @Schema(description = "사진리스트")
    private List<MultipartFile> images = new ArrayList<>();

    @Schema(description = "게시판 카테고리")
    private CategoryType categoryType;

    public Board toBoard(Member loginMember, Category category){
        return Board.builder()
                .title(title)
                .content(content)
                .member(loginMember)
                .tag(Board.toDBTag(tags))
                .status(BoardStatus.S)
                .category(category)
                .totalScore(0)
                .build();
    }

}
