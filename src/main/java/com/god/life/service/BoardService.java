package com.god.life.service;

import com.god.life.domain.*;
import com.god.life.dto.*;
import com.god.life.error.ErrorMessage;
import com.god.life.error.ForbiddenException;
import com.god.life.error.NotFoundResource;
import com.god.life.repository.BoardRepository;
import com.god.life.repository.CategoryRepository;
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
    private final GodLifeScoreService godLifeScoreService;
    private final CategoryRepository categoryRepository;
    //private final WeeklyPopularBoardCacheService weeklyPopularBoardCacheService;
    //private final RedisTemplate<String, Object> redisTemplate;

    private static final String POPULAR_BOARDS_KEY = "popularBoard";


    @Transactional
    public Long createBoard(BoardCreateRequest request, Member loginMember, List<ImageSaveResponse> uploadResponse) {
        Category category = categoryRepository.findByCategoryType(CategoryType.GOD_LIFE_PAGE);
        // DB entity 생성
        Board board = request.toBoard(loginMember,category);
        boardRepository.save(board);


        for (ImageSaveResponse response : uploadResponse) {
            imageService.saveImage(response, loginMember, board);
        }

        //생성된 게시판 ID 반환
        return board.getId();
    }


    @Transactional
    public BoardResponse detailBoard(Long boardId, Member loginMember) {
        Board board = boardRepository.findByIdWithMember(boardId, CategoryType.GOD_LIFE_PAGE)
                .orElseThrow(() -> new NotFoundResource(ErrorMessage.INVALID_BOARD_MESSAGE.getErrorMessage()));

        board.increaseViewCount();
        boolean isOwner = board.getMember().getId().equals(loginMember.getId()); // 작성자와 현재 로그인한 사람이 동일인인지
        boolean memberLikedBoard = godLifeScoreService.isMemberLikedBoard(board, loginMember);

        return BoardResponse.of(board, isOwner, memberLikedBoard);
    }


    public void checkAuthorization(Member member, Long boardId) {
        Board board = boardRepository.findByIdWithMember(boardId, CategoryType.GOD_LIFE_PAGE)
                .orElseThrow(() -> new NotFoundResource(ErrorMessage.INVALID_BOARD_MESSAGE.getErrorMessage()));

        if (!member.getId().equals(board.getMember().getId())) {
            throw new ForbiddenException("수정 및 삭제 권한이 없습니다.");
        }

    }

    @Transactional
    public BoardResponse updateBoard(Long boardId, List<ImageSaveResponse> uploadResponse, BoardCreateRequest request, Member loginMember) {
        Board board = boardRepository.findById(boardId).get(); //권한 체크 로직에서 게시판이 있는지 확인하므로 바로 꺼내오기

        imageService.deleteImages(boardId); // 이미지 삭제후 다시 저장

        for (ImageSaveResponse response : uploadResponse) {
            imageService.saveImage(response, loginMember, board);
        }

        board.updateBoard(request);
        boolean memberLikedBoard = godLifeScoreService.isMemberLikedBoard(board, loginMember);

        return BoardResponse.of(board, true, memberLikedBoard);
    }

    @Transactional
    public boolean deleteBoard(Long boardId) {
        boardRepository.deleteById(boardId);
        return true;
    }

//    @Transactional(readOnly = true)
//    public List<BoardSearchResponse> getBoardList(BoardSearchRequest boardSearchRequest) {
//        Pageable pageable =
//                PageRequest
//                        .of((boardSearchRequest.getPage() - 1), 10, Sort.by("createDate").descending());
//
//        Page<Board> pagingBoard = boardRepository.findByBoardfetchjoin(pageable);
//
//        List<Board> boards = pagingBoard.getContent();
//
//        boards.stream()
//                .forEach(b -> {
//                    b.getComments();
//                    b.getImages();
//                    b.getMember().getImages();
//                });
//
//
//        return boards.stream().map(b -> BoardSearchResponse.of(b, false)).toList();
//    }

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
                    b.getGodLifeScores();
                    b.getMember().getImages();
                });

        List<BoardSearchResponse> response = new ArrayList<>();
        for (Board board : boards) {
            BoardSearchResponse dto = BoardSearchResponse.of(board, false);
            dto.setGodScore(board.getGodLifeScores().stream().mapToInt(GodLifeScore::getScore).sum());
            response.add(dto);
        }

        return response;
    }

    public void deleteBoardWrittenByMember(Member deteleMember) {
        boardRepository.deleteByMember(deteleMember);
    }

    @Transactional(readOnly = true)
    public List<BoardSearchResponse> searchWeeklyPopularBoardList() {
        return boardRepository.findWeeklyPopularBoard();
        // return (List<BoardSearchResponse>) redisTemplate.opsForValue().get(POPULAR_BOARDS_KEY);
    }

    @Transactional(readOnly = true)
    public List<BoardSearchResponse> searchTopPopularBoardList(){
        return boardRepository.findTotalPopularBoard();
    }


    @Transactional(readOnly = true)
    public void cachingWeeklyBoard(){
        List<BoardSearchResponse> weeklyPopularBoard = boardRepository.findWeeklyPopularBoard();
        //redisTemplate.opsForValue().set(POPULAR_BOARDS_KEY, weeklyPopularBoard);
    }


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


    public GodLifeStimulationBoardResponse detailStimulusBoard(Long boardId, Member member) {

        return boardRepository.findStimulusBoardEqualsBoardId(boardId, member);
    }

    public List<GodLifeStimulationBoardBriefResponse> getListStimulusBoard(Integer page) {
        Page<GodLifeStimulationBoardBriefResponse> result = boardRepository
                .findStimulusBoardPaging(PageRequest.of(page, PAGE_SIZE, Sort.by("create_date").descending()));

        return result.getContent();
    }

    @Transactional
    public List<Long> getIncompleteWriteBoardIds(LocalDateTime date) {
        List<Long> incompleteBoardsBeforeDate = boardRepository.findIncompleteBoardsBeforeDate(date,BoardStatus.T, CategoryType.GOD_LIFE_STIMULUS);
        //List<Long> incompleteBoardsBeforeDate = boardRepository.findIncompleteBoardsBeforeDate(date);
        imageService.deleteImages(incompleteBoardsBeforeDate);
        boardRepository.deleteAllById(incompleteBoardsBeforeDate);
        return incompleteBoardsBeforeDate;
    }

    public List<GodLifeStimulationBoardBriefResponse> getListStimulusBoardUsingSearchCondition(StimulationBoardSearchCondition request) {
        return boardRepository.findStimulusBoardSearchCondition(request);
    }

    public List<GodLifeStimulationBoardBriefResponse> getAllTimePopularStimulusBoardList(){
        return boardRepository.findAllTimePopularStimulusBoardList();
    }

    public List<GodLifeStimulationBoardBriefResponse> getMostViewedStimulusBoardList(){
        return boardRepository.findMostViewedBoardList();
    }
}
