package com.god.life.controller;

import com.god.life.annotation.LoginMember;
import com.god.life.domain.Member;
import com.god.life.dto.BoardCreateRequest;
import com.god.life.dto.BoardResponse;
import com.god.life.dto.ImageSaveResponse;
import com.god.life.dto.common.CommonResponse;
import com.god.life.exception.NotFoundResource;
import com.god.life.service.BoardService;
import com.god.life.service.ImageService;
import com.god.life.service.ImageUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "게시판 업로드 관련 API", description = "게시판 CRUD API입니다.")
@RestController
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;
    private final ImageService imageService;
    private final ImageUploadService imageUploadService;

    @Operation(summary = "게시판 생성")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "게시판 생성 성공시 생성된 게시판 번호 반환",
                            useReturnTypeSchema = true)
            }
    )
    @PostMapping("/board")
    public ResponseEntity<CommonResponse<Long>> createBoard(@ModelAttribute BoardCreateRequest request,
                                                            @LoginMember Member member) {

        List<ImageSaveResponse> uploadResponse = imageUploadService.uploads(request.getImages());
        Long boardId = boardService.createBoard(request, member, uploadResponse);

        return ResponseEntity.ok().body(new CommonResponse<>(HttpStatus.CREATED, boardId));
    }


    @Operation(summary = "게시판 상세 조회")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "조회하는 게시판 정보 반환",
                            useReturnTypeSchema = true)
            }
    )
    @GetMapping("/board/{id}")
    public ResponseEntity<CommonResponse<BoardResponse>> viewBoard(@PathVariable(name = "id") String id,
                                                                   @LoginMember Member member) {

        Long boardId = checkId(id);
        BoardResponse boardResponse = boardService.detailBoard(boardId, member);
        List<ImageSaveResponse> images = imageService.findImages(boardId);
        boardResponse.setImagesURL(images.stream().map(ImageSaveResponse::getServerName).toList());

        return ResponseEntity.ok(new CommonResponse<>(HttpStatus.OK, boardResponse));
    }

    @Operation(summary = "게시판 수정")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "수정된 게시판 정보 반환",
                            useReturnTypeSchema = true)
            }
    )
    @PutMapping("/board/{id}")
    public ResponseEntity<CommonResponse<BoardResponse>> updateBoard(@PathVariable(name = "id") String id,
                                                                     @ModelAttribute BoardCreateRequest request,
                                                                     @LoginMember Member member) {
        Long boardId = checkId(id);
        boardService.checkAuthorization(member, boardId);

        imageService.deleteImages(boardId); //기존 이미지 삭제
        List<ImageSaveResponse> images = imageUploadService.uploads(request.getImages());

        BoardResponse boardResponse = boardService.updateBoard(boardId, images, request, member);
        boardResponse.setImagesURL(images.stream().map(ImageSaveResponse::getServerName).toList());

        return ResponseEntity.ok(new CommonResponse<>(HttpStatus.OK, boardResponse));
    }

    @Operation(summary = "게시판 삭제")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "게시판 삭제, 성공시 True",
                            useReturnTypeSchema = true)
            }
    )
    @DeleteMapping("/board/{id}")
    public ResponseEntity<CommonResponse<Boolean>> deleteBoard(@PathVariable(name = "id") String id,
                                                               @LoginMember Member member)
    {
        Long boardId = checkId(id);
        boardService.checkAuthorization(member, boardId);

        imageService.deleteImages(boardId); //기존 이미지 삭제
        return ResponseEntity.ok(new CommonResponse<>(HttpStatus.OK,boardService.deleteBoard(boardId), ""));
    }


    private Long checkId(String id) {
        long boardId;
        try {
            boardId = Long.parseLong(id);
        } catch (NumberFormatException ex) {
            throw new NotFoundResource("존재하지 않는 게시물입니다.");
        }

        return boardId;
    }



}