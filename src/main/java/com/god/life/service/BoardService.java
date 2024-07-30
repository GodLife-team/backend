package com.god.life.service;

import com.god.life.domain.*;
import com.god.life.dto.board.request.BoardCreateRequest;
import com.god.life.dto.board.request.BoardSearchRequest;
import com.god.life.dto.board.request.GodLifeStimulationBoardRequest;
import com.god.life.dto.board.response.BoardResponse;
import com.god.life.dto.board.response.BoardSearchResponse;
import com.god.life.dto.board.response.GodLifeStimulationBoardBriefResponse;
import com.god.life.dto.board.response.GodLifeStimulationBoardResponse;
import com.god.life.dto.image.ImageSaveResponse;
import com.god.life.dto.board.request.StimulationBoardSearchCondition;
import com.god.life.error.ErrorMessage;
import com.god.life.error.ForbiddenException;
import com.god.life.error.NotFoundResource;
import com.god.life.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BoardService {

    private static final int PAGE_SIZE = 10;
    private final BoardRepository boardRepository;
    private final ImageService imageService;
    private final CategoryRepository categoryRepository;
    private final GodLifeScoreRepository godLifeScoreRepository;
    private final CommentRepository commentRepository;


    /**
     * @param request 갓생 인증 게시물 정보
     * @param loginMember 작성자
     * @param uploadResponse 업로드한 이미지 정보
     * @return 해당 게시물 저장 번호
     */
    @Transactional
    public Long createBoard(BoardCreateRequest request, Member loginMember, List<ImageSaveResponse> uploadResponse) {
            Category category = categoryRepository.findByCategoryType(CategoryType.GOD_LIFE_PAGE);
            // DB entity 생성
            Board board = request.toBoard(loginMember,category);
        boardRepository.save(board);

        for (ImageSaveResponse response : uploadResponse) {
            imageService.saveImage(response, loginMember, board);
        }

        GodLifeScore godLifeScore = GodLifeScore.likeMemberToBoard(loginMember, board);
        godLifeScoreRepository.save(godLifeScore);
        //생성된 게시판 ID 반환
        return board.getId();
    }


    /** 갓생 인증 게시글 상세 조회
     * @param boardId 상세 조회할 게시물 번호
     * @param loginMember 현재 접속 중인 유저 정보
     * @return 해당 게시물 상세 정보 반환
     */
    @Transactional
    public BoardResponse detailBoard(Long boardId, Member loginMember) {
        Board board = boardRepository.findByIdWithMember(boardId, CategoryType.GOD_LIFE_PAGE)
                .orElseThrow(() -> new NotFoundResource(ErrorMessage.INVALID_BOARD_MESSAGE.getErrorMessage()));

        board.increaseViewCount();
        boolean isOwner = board.getMember().getId().equals(loginMember.getId()); // 작성자와 현재 로그인한 사람이 동일인인지
        boolean memberLikedBoard = godLifeScoreRepository.existsByBoardAndMember(board, loginMember);

        return BoardResponse.of(board, isOwner, memberLikedBoard);
    }


    /**
     * @param member  현재 로그인한 유저 정보 (게시판 권한 확인)
     * @param boardId 조회할 게시물 번호
     */
    public void checkAuthorization(Member member, Long boardId) {
        Board board = boardRepository.findByIdWithMember(boardId, CategoryType.GOD_LIFE_PAGE)
                .orElseThrow(() -> new NotFoundResource(ErrorMessage.INVALID_BOARD_MESSAGE.getErrorMessage()));

        if (!member.getId().equals(board.getMember().getId())) {
            throw new ForbiddenException(ErrorMessage.FORBIDDEN_ACTION_MESSAGE.getErrorMessage());
        }

    }

    /**
     * @param boardId 수정할 게시판 번호
     * @param uploadResponse 다시 업로드한 이미지 정보
     * @param request 수정할 게시판 정보
     * @param loginMember 현재 접속 중인 유저
     * @return
     */
    @Transactional
    public BoardResponse updateBoard(Long boardId, List<ImageSaveResponse> uploadResponse, BoardCreateRequest request, Member loginMember) {
        Board board = boardRepository.findById(boardId).get(); //권한 체크 로직에서 게시판이 있는지 확인하므로 바로 꺼내오기

        imageService.deleteImages(boardId); // 이미지 삭제후 다시 저장

        for (ImageSaveResponse response : uploadResponse) {
            imageService.saveImage(response, loginMember, board);
        }

        board.updateBoard(request);
        boolean memberLikedBoard = godLifeScoreRepository.existsByBoardAndMember(board, loginMember);

        return BoardResponse.of(board, true, memberLikedBoard);
    }

    /**
     * @param boardId 삭제할 게시판 번호
     * @return 삭제 성공 유무
     */
    @Transactional
    public boolean deleteBoard(Long boardId) {
        commentRepository.deleteByBoardId(boardId);
        godLifeScoreRepository.deleteByBoardId(boardId);
        imageService.deleteImages(boardId);
        boardRepository.deleteById(boardId);
        return true;
    }

    /**
     * @param boardSearchRequest 갓생 인증 게시물 검색 조건
     * @return  검색 조건에 맞는 갓생 인증 게시물 리스트 반환
     */
    @Transactional(readOnly = true)
    public List<BoardSearchResponse> getBoardList(BoardSearchRequest boardSearchRequest) {
        Pageable pageable =
                PageRequest
                        .of((boardSearchRequest.getPage() - 1), PAGE_SIZE, Sort.by("createDate").descending());

        Page<Board> pagingBoard = boardRepository.findBoardWithSearchRequest(boardSearchRequest, pageable);
        List<Board> boards = pagingBoard.getContent();

        boards.stream()
                .forEach(b -> {
                    b.getComments();
                    b.getImages();
                });

        List<BoardSearchResponse> response = new ArrayList<>();
        for (Board board : boards) {
            BoardSearchResponse dto = BoardSearchResponse.of(board, false);
            dto.substractPoint(2);
            response.add(dto);
        }

        return response;
    }

    /**
     * 회원 탈퇴한 유저의 게시물을 삭제합니다.
     * @param deteleMember 회원탈퇴한 유저 번호
     */
    public void deleteBoardWrittenByMember(Member deteleMember) {
        boardRepository.deleteByMember(deteleMember);
    }

    /**
     * 한 주간 인기 있는 갓생 인증 게시물 리스트 반환
     */
    @Transactional(readOnly = true)
    public List<BoardSearchResponse> searchWeeklyPopularBoardList() {
        return boardRepository.findWeeklyPopularBoard();
    }

    /**
     * 전체 기간 동안 인기 있는 갓생 인증 게시물 반환
     */
    @Transactional(readOnly = true)
    public List<BoardSearchResponse> searchTopPopularBoardList(){
        return boardRepository.findTotalPopularBoard();
    }

    /**
     * @param member : 임시 게시물을 작성할 회언
     * @return 임시 게시물 번호 반환
     */
    @Transactional
    public Long createTemporaryBoard(Member member) {
        Category category = categoryRepository.findByCategoryType(CategoryType.GOD_LIFE_STIMULUS);

        Board tmpBoard = Board.builder()
                .title(null)
                .content(null)
                .tag("")
                .thumbnailUrl("")
                .member(member)
                .totalScore(0)
                .view(0)
                .category(category)
                .status(BoardStatus.T)
                .build();

        return boardRepository.save(tmpBoard).getId();
    }

    /**
     * @param member 작성자
     * @param dto 최종 갓생 자극 인증 게시물 반환
     * @return 게시물 번호 반환
     */
    @Transactional
    public Long saveTemporaryBoard(Member member, GodLifeStimulationBoardRequest dto) {
        Long boardId = dto.getBoardId();
        Board board = boardRepository.findTemporaryBoardByIdAndBoardStatus(boardId, BoardStatus.T)
                .orElseThrow(() -> new NotFoundResource(ErrorMessage.INVALID_BOARD_MESSAGE.getErrorMessage()));

        //권한 체크 ==> 최종적으로 마무리할 수 있는지
        if (!board.getMember().getId().equals(member.getId())) {
            throw new ForbiddenException(ErrorMessage.FORBIDDEN_ACTION_MESSAGE.getErrorMessage());
        }

        board.updateBoard(dto);
        return board.getId();
    }

    /**
     * @param boardId 조회할 갓생 자극 게시물 번호
     * @param member 로그인 유저
     * @return 갓생 자극 게시물 상세 정보
     */
    @Transactional
    public GodLifeStimulationBoardResponse detailStimulusBoard(Long boardId, Member member) {
        return boardRepository.findStimulusBoardEqualsBoardId(boardId, member);
    }

    /**
     * @param page : 페이지 번호
     * @return 페이지 번호에 맞는 갓생 자극 게시물 간단한 정보 반ㄴ환
     */
    public List<GodLifeStimulationBoardBriefResponse> getListStimulusBoard(Integer page) {
        Page<GodLifeStimulationBoardBriefResponse> result = boardRepository
                .findStimulusBoardPaging(PageRequest.of(page, PAGE_SIZE, Sort.by("create_date").descending()));

        return result.getContent();
    }

    /**
     * @param date 임시 게시물을 삭제할 날짜 기쥰
     * @return 삭제된 임기 게시물 번호
     */
    @Transactional
    public List<Long> getIncompleteWriteBoardIds(LocalDateTime date) {
        List<Long> incompleteBoardsBeforeDate = boardRepository
                .findIncompleteBoardsBeforeDate(date,BoardStatus.T, CategoryType.GOD_LIFE_STIMULUS);
        imageService.deleteImages(incompleteBoardsBeforeDate);
        boardRepository.deleteAllById(incompleteBoardsBeforeDate);
        return incompleteBoardsBeforeDate;
    }

    /**
     * @param request 갓생 자극 게시물 검색 조건
     * @return 검색 조건에 맞는 갓생 자극 게시물 반환
     */
    public List<GodLifeStimulationBoardBriefResponse> getListStimulusBoardUsingSearchCondition(StimulationBoardSearchCondition request) {
        return boardRepository.findStimulusBoardSearchCondition(request);
    }

    /**
     * @return 전체기간 동안 가장 인기있는 갓생 자극 게시물 리스트 반환
     */
    public List<GodLifeStimulationBoardBriefResponse> getAllTimePopularStimulusBoardList(){
        return boardRepository.findAllTimePopularStimulusBoardList();
    }

    /**
     * @return 전체기간 동안 가장 조회수가 높은 갓생 자극 게시물 리스트 반환
     */
    public List<GodLifeStimulationBoardBriefResponse> getMostViewedStimulusBoardList(){
        return boardRepository.findMostViewedBoardList();
    }

    /**
     * @param loginMember 로그인한 유저
     * @return 로그인한 유저의 전체 갓생 인정 점수 반환
     */
    public int calculateGodLifeScoreMember(Member loginMember) {
        Integer sum = boardRepository.totalScoreBoardByLoginMember(loginMember);
        return sum == null ? 0 : sum;
    }

    /**
     * 갓생 자극 게시물 수정사항 반영
     *
     * @param member  : 작성자
     * @param request : 수정 내용 본
     * @return 업데이트된 게시판 번호 반환
     */
    @Transactional
    public Long updateStimulationBoard(Member member, GodLifeStimulationBoardRequest request) {
        Board board = boardRepository.findByIdWithMember(request.getBoardId(), CategoryType.GOD_LIFE_STIMULUS)
                .orElseThrow(() -> new NotFoundResource(ErrorMessage.INVALID_BOARD_MESSAGE.getErrorMessage()));

        //권한 체크 ==> 최종적으로 마무리할 수 있는지
        if (!board.getMember().getId().equals(member.getId())) {
            throw new ForbiddenException(ErrorMessage.FORBIDDEN_ACTION_MESSAGE.getErrorMessage());
        }

        board.updateBoard(request);
        return board.getId();
    }
}
