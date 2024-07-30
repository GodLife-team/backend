package com.god.life.controller;

import com.god.life.annotation.LoginMember;
import com.god.life.domain.Member;
import com.god.life.dto.board.request.BoardCreateRequest;
import com.god.life.dto.board.request.BoardSearchRequest;
import com.god.life.dto.board.request.GodLifeStimulationBoardRequest;
import com.god.life.dto.board.request.StimulationBoardSearchCondition;
import com.god.life.dto.board.response.BoardResponse;
import com.god.life.dto.board.response.BoardSearchResponse;
import com.god.life.dto.board.response.GodLifeStimulationBoardBriefResponse;
import com.god.life.dto.board.response.GodLifeStimulationBoardResponse;
import com.god.life.dto.common.CommonResponse;
import com.god.life.dto.image.ImageSaveResponse;
import com.god.life.error.NotFoundResource;
import com.god.life.service.BoardService;
import com.god.life.service.GodLifeScoreService;
import com.god.life.service.ImageService;
import com.god.life.service.ImageUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "게시판 업로드 관련 API", description = "게시판 CRUD API입니다.")
@RestController
@RequiredArgsConstructor
@Slf4j
public class BoardController {

    private final BoardService boardService;
    private final ImageService imageService;
    private final ImageUploadService imageUploadService;
    private final GodLifeScoreService godLifeScoreService;

    @Operation(summary = "게시판 생성")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "게시판 생성 성공시 생성된 게시판 번호 반환",
                            useReturnTypeSchema = true)
            }
    )
    @PostMapping(value = "/board", consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommonResponse<Long>> createBoard(@ModelAttribute BoardCreateRequest request,
                                                            @LoginMember Member member) {
        long start = System.currentTimeMillis();
        log.info("게시판 생성 시작");
        List<ImageSaveResponse> uploadResponse = imageUploadService.uploads(request.getImages());
        Long boardId = boardService.createBoard(request, member, uploadResponse);

        long end = System.currentTimeMillis();
        log.info("게시판 생성 종료 걸리는 시간 = {}ms", (end - start));
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
        int godLifeScore = godLifeScoreService.calculateGodLifeScoreBoard(boardId);
        boardResponse.setGodScore(godLifeScore);

        return ResponseEntity.ok(new CommonResponse<>(HttpStatus.OK, boardResponse));
    }

    @Operation(summary = "게시판 수정")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "수정된 게시판 정보 반환",
                            useReturnTypeSchema = true)
            }
    )
    @PutMapping(value = "/board/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommonResponse<Long>> updateBoard(@PathVariable(name = "id") String id,
                                                                     @ModelAttribute BoardCreateRequest request,
                                                                     @LoginMember Member member) {
        Long boardId = checkId(id);
        boardService.checkAuthorization(member, boardId);

        imageService.deleteImages(boardId); //기존 이미지 삭제
        List<ImageSaveResponse> images = imageUploadService.uploads(request.getImages());

        BoardResponse boardResponse = boardService.updateBoard(boardId, images, request, member);
        boardResponse.setImagesURL(images.stream().map(ImageSaveResponse::getServerName).toList());

        return ResponseEntity.ok(new CommonResponse<>(HttpStatus.OK, boardId));
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


        return ResponseEntity.ok(new CommonResponse<>(HttpStatus.OK,boardService.deleteBoard(boardId), ""));
    }


    @GetMapping("/boards")
    @Operation(summary = "검색 조건에 따른 갓생 인증 게시물 조회")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "최신 게시판 검색 조회",
                    useReturnTypeSchema = true)
            }
    )
    public ResponseEntity<CommonResponse<List<BoardSearchResponse>>> getBoardList(
            @Valid @ModelAttribute BoardSearchRequest boardSearchRequest) {
        log.info("requestDTO = {}", boardSearchRequest);
        if (boardSearchRequest.getPage() == null) { // 없으면 0번 페이지조회하도록
            boardSearchRequest.setPage(1);
        }

        List<BoardSearchResponse> boardList = boardService.getBoardList(boardSearchRequest);

        return ResponseEntity.ok(new CommonResponse<>(HttpStatus.OK, boardList));
    }

    @PostMapping("/board/tmp")
    @Operation(summary = "임시 테이블 생성")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "임시 저장 게시판 PK 값 전달",
                            useReturnTypeSchema = true)
            }
    )
    public ResponseEntity<CommonResponse<Long>> postTemporaryBoard(@LoginMember Member member) {
        Long temporaryBoardId = boardService.createTemporaryBoard(member);
        return ResponseEntity.ok(new CommonResponse<>(HttpStatus.OK, temporaryBoardId));
    }

    @PostMapping("/board/stimulation")
    @Operation(summary = "임시 갓생 자극 게시물 저장 처리")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "갓생 자극 게시판 저장처리",
                            useReturnTypeSchema = true)
            }
    )
    public ResponseEntity<CommonResponse<Long>> postStimulationBoard(@LoginMember Member member,
                                                                     @Parameter(description = "갓생 자극 게시물 최종 작성") @RequestBody GodLifeStimulationBoardRequest request) {

        Long savedBoardId = boardService.saveTemporaryBoard(member, request);
        imageService.deleteUnusedImageInHtml(request.getContent(), savedBoardId, request.getThumbnailUrl());

        return ResponseEntity.ok(new CommonResponse<>(HttpStatus.OK, savedBoardId));
    }

    @PutMapping("/board/stimulation")
    @Operation(summary = "갓생 자극 게시물 수정")
    @ApiResponse(responseCode = "200", description = "수정 성공시 수정된 게시판 ID 반환")
    public ResponseEntity<CommonResponse<Long>> updateBoard(@LoginMember Member member,
                                                            @Parameter(description = "갓생 자극 게시물 수정본") @RequestBody GodLifeStimulationBoardRequest request) {
        Long updatedBoardId = boardService.updateStimulationBoard(member, request);
        imageService.deleteUnusedImageInHtml(request.getContent(), updatedBoardId, request.getThumbnailUrl());

        return ResponseEntity.ok(new CommonResponse<>(HttpStatus.OK, updatedBoardId));
    }

    @GetMapping("/board/stimulation/{boardId}")
    @Operation(summary = "갓생 자극 게시물 상세 조회")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "갓생 자극 게시판 조회",
                            useReturnTypeSchema = true)
            }
    )
    public ResponseEntity<CommonResponse<GodLifeStimulationBoardResponse>> viewGodStimulusBoard(
            @PathVariable(name = "boardId") String boardId,
            @LoginMember Member member) {
        GodLifeStimulationBoardResponse response
                = boardService.detailStimulusBoard(checkId(boardId), member);

        return ResponseEntity.ok(new CommonResponse<>(HttpStatus.OK, response));
    }

    @GetMapping("/boards/stimulation")
    @Operation(summary = "갓생 자극 게시물 조회 (페이징 처리)")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "갓생 자극 게시판 필터링 조회",
                            useReturnTypeSchema = true)
            }
    )
    public ResponseEntity<CommonResponse<List<GodLifeStimulationBoardBriefResponse>>> viewGodStimulusBoardListUsingPaging(
            @Parameter(description = "갓생 자극 게시물 페이징 검색 번호 0부터 시작") Integer page
    ) {
        List<GodLifeStimulationBoardBriefResponse> response = boardService.getListStimulusBoard(page);
        return ResponseEntity.ok(new CommonResponse<>(HttpStatus.OK, response));
    }


    @GetMapping("/boards/stimulation/filter")
    @Operation(summary = "검색 조건에 맞는 갓생 자극 게시물 조회")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "조건 검색에 맞는 게시물 조회, 맞는 게시물이 없다면 빈 리스트 반환",
                    useReturnTypeSchema = true)
            }
    )
    public ResponseEntity<CommonResponse<List<GodLifeStimulationBoardBriefResponse>>> viewGodStimulusBoardListUsingFilter(
           @Parameter(in = ParameterIn.QUERY, description = "조건 검색") StimulationBoardSearchCondition request
    ) {
        List<GodLifeStimulationBoardBriefResponse> response = boardService.getListStimulusBoardUsingSearchCondition(request);
        return ResponseEntity.ok(new CommonResponse<>(HttpStatus.OK, response));
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
