package com.god.life.controller;


import com.god.life.annotation.LoginMember;
import com.god.life.domain.Member;
import com.god.life.dto.comment.request.CommentCreateRequest;
import com.god.life.dto.comment.response.CommentResponse;
import com.god.life.dto.common.CommonResponse;
import com.god.life.error.NotFoundResource;
import com.god.life.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "댓글 관련 API", description = "댓글  CRUD API입니다.")
@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;


    @Operation(summary = "댓글 조회")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "해당 게시판 번호에 따른 댓글 반환," +
                            " 작성날짜를 기준으로 오름차순 정렬하여 반환",
                            useReturnTypeSchema = true)
            }
    )
    @Parameter(name="Authorization", description = "Bearer {Access Token}형태", required = true)
    @GetMapping("/comment/{boardId}")
    public ResponseEntity<CommonResponse<List<CommentResponse>>> getComments(
            @PathVariable("boardId") String boardId,
            @LoginMember Member member){
        Long id = checkId(boardId);
        List<CommentResponse> commentsForBoard = commentService.getCommentsForBoard(id, member);

        return ResponseEntity.ok(new CommonResponse<>(HttpStatus.OK, commentsForBoard));
    }

    @Operation(summary = "댓글 등록")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "게시판 번호에 대한 댓글 작성, 성공시 true",
                            useReturnTypeSchema = true)
            }
    )
    @Parameter(name="Authorization", description = "Bearer {Access Token}형태", required = true)
    @PostMapping("/comment/{boardId}")
    public ResponseEntity<CommonResponse<Boolean>> createComment(
            @PathVariable("boardId") String boardId,
            @RequestBody CommentCreateRequest request,
            @LoginMember Member member) {

        Long id = checkId(boardId);
        CommentResponse commentResponse = commentService.createComment(id, request, member);

        return ResponseEntity.ok(new CommonResponse<>(HttpStatus.OK, true));
    }

    @Operation(summary = "댓글 수정")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "댓글 번호에 따른 댓글 수정, 성공시 true",
                            useReturnTypeSchema = true)
            }
    )
    @Parameter(name="Authorization", description = "Bearer {Access Token}형태", required = true)
    @PutMapping("/comment/{commentId}")
    public ResponseEntity<CommonResponse<Boolean>> updateComment(
            @PathVariable("commentId") String commentId,
            @RequestBody CommentCreateRequest request,
            @LoginMember Member member) {

        Long id = checkId(commentId);
        CommentResponse commentResponse = commentService.updateComment(id, request, member);

        return ResponseEntity.ok(new CommonResponse<>(HttpStatus.OK, true));
    }

    @Operation(summary = "댓글 삭제")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "삭제 성공시 True 반환",
                            useReturnTypeSchema = true)
            }
    )
    @Parameter(name="Authorization", description = "Bearer {Access Token}형태", required = true)
    @DeleteMapping("/comment/{commentId}")
    public ResponseEntity<CommonResponse<Boolean>> updateComment(
            @PathVariable("commentId") String commentId,
            @LoginMember Member member) {

        Long parseId = checkId(commentId);
        commentService.deleteComment(parseId, member);

        return ResponseEntity.ok(new CommonResponse<>(HttpStatus.OK, true));
    }

    private Long checkId(String id) {
        long result;
        try {
            result = Long.parseLong(id);
        } catch (NumberFormatException ex) {
            throw new NotFoundResource("존재하지 않는 게시판입니다.");
        }

        return result;
    }

}
