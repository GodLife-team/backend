package com.god.life.dto;


import com.god.life.domain.Board;
import com.god.life.domain.Comment;
import com.god.life.domain.Member;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentCreateRequest {

    @Schema(description = "댓글 내용 최대 255자")
    @Max(value = 255, message = "댓글 내용이 너무 깁니다.")
    private String comment;

    public Comment toEntity(Board board, Member member) {
        return Comment.builder()
                .replyContent(comment)
                .board(board)
                .member(member)
                .build();
    }

}
