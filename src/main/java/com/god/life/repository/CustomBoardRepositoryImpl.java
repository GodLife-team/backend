package com.god.life.repository;

import com.god.life.domain.*;
import com.god.life.dto.*;
import com.god.life.error.ErrorMessage;
import com.god.life.error.NotFoundResource;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.god.life.domain.QBoard.board;
import static com.god.life.domain.QCategory.category;
import static com.god.life.domain.QGodLifeScore.godLifeScore;
import static com.god.life.domain.QMember.member;

@Repository
@Slf4j
public class CustomBoardRepositoryImpl implements CustomBoardRepository {

    private final JPAQueryFactory queryFactory;

    public CustomBoardRepositoryImpl(EntityManager em){
        queryFactory = new JPAQueryFactory(em);
    }

    /**
     * 한주간 갓생 인정을 가장 많이 받은 갓생 인증 게시물 10개를 반환합니다.
     * @return 갓생 인증 리스트 10개
     */
    @Override
    public List<BoardSearchResponse> findWeeklyPopularBoard() {
        long start = System.currentTimeMillis();
        log.info("한주간 인기 있는 갓생 인정 게시물 조회 시작");
        LocalDateTime today = LocalDateTime.now(); // 현재 시각
        //이번 주 월요일 0시 0분 0초
        LocalDateTime monday = LocalDateTime.of(LocalDate.now().with(DayOfWeek.MONDAY), LocalTime.MIDNIGHT);

        //인기 있는 게시물 번호 조회 (좋아요 수까지)
        List<PopularBoardQueryDTO> weeklyPopularBoardDTO = queryFactory.select(Projections.bean(
                        PopularBoardQueryDTO.class,
                        board.id.as("boardId"),
                        godLifeScore.score.sum().as("sum")
                ))
                .from(board)
                .join(category).on(board.category.categoryId.eq(category.categoryId))
                .join(godLifeScore).on(board.id.eq(godLifeScore.board.id))
                .where(
                        //일주일 간격으로 수행
                        godLifeScore.createDate.between(monday, today).and(
                                category.categoryType.eq(CategoryType.GOD_LIFE_PAGE)
                        )
                )
                .groupBy(board.id)
                .orderBy(godLifeScore.score.sum().desc(), board.id.asc())
                .having(godLifeScore.score.sum().gt(2))
                .offset(0)
                .limit(10) // 10개만 fetch
                .fetch();

        //인기 있는 게시판 정보 조회
        List<Board> boards = queryFactory.selectFrom(board)
                .join(board.member, member).fetchJoin()
                .where(
                        board.id.in(weeklyPopularBoardDTO.stream()
                                .map(PopularBoardQueryDTO::getBoardId).collect(Collectors.toList())))
                .orderBy(board.id.asc())
                .fetch();

        // In 절 쿼리
        boards.stream().map(Board::getComments).forEach(Hibernate::initialize); // 댓글 fetch 조인
        boards.stream().map(Board::getImages).forEach(Hibernate::initialize); // 이미지 fetch 조인

        //조립
        List<BoardSearchResponse> result = new ArrayList<>();
        for (int i = 0; i < boards.size(); i++) {
            BoardSearchResponse dto = BoardSearchResponse.of(boards.get(i), false);
            dto.setGodScore(weeklyPopularBoardDTO.get(i).getSum());
            result.add(dto);
        }

        long end = System.currentTimeMillis();
        log.info("한 주간 인기 있는 갓생 인정 게시물 조회 종료 시간 = {}", (end-start)/(double)1000);

        return result;
    }


    /**
     * 전체 기간에서 갓생 인정을 가장 많이 받은 게시물 10개를 반환합니다.
     */
    @Override
    public List<BoardSearchResponse> findTotalPopularBoard() {
        long start = System.currentTimeMillis();
        log.info("전체 기간 인기 있는 갓생 인정 게시물 조회 시작");
        List<Board> boards = queryFactory.selectFrom(board)
                .join(board.member, member).fetchJoin()
                .where(category.categoryType.eq(CategoryType.GOD_LIFE_PAGE)) //게시글 작성으로 받은 점수는 제외
                .orderBy(board.totalScore.desc(), board.id.asc())
                .offset(0)
                .limit(10)
                .fetch();

        // In 절 쿼리 -> 1:N 여러번을 할 수 없으므로
        boards.stream().map(Board::getComments).forEach(Hibernate::initialize); // 댓글 fetch 조인 --> 서브쿼리로 빼기
        boards.stream().map(Board::getImages).forEach(Hibernate::initialize); // 이미지 fetch 조인

        //조립
        List<BoardSearchResponse> result = new ArrayList<>();
        for (int i = 0; i < boards.size(); i++) {
            BoardSearchResponse dto = BoardSearchResponse.of(boards.get(i), false);
            dto.substractPoint(Board.WRITE_POINT);
            result.add(dto);
        }


        long end = System.currentTimeMillis();
        log.info("전체 기간 인기 있는 갓생 인정 게시물 조회 종료 시간 = {}", (end-start)/(double)1000);
        return result;
    }

    /**
     * @param boardSearchRequest 검색 조건
     * @param pageable 페이징 번호 및 정렬 깆누
     * @return 요청에 적합한 게시물 리스트 반환
     */
    @Override
    public Page<Board> findBoardWithSearchRequest(BoardSearchRequest boardSearchRequest, Pageable pageable) {
        List<Board> boards = queryFactory.selectFrom(board)
                .join(category).on(board.category.categoryId.eq(category.categoryId))
                .join(QBoard.board.member, member).fetchJoin()
                .where(keywordParam(boardSearchRequest.getKeyword()),tagsParam(boardSearchRequest.getTags()),
                        nicknameParam(boardSearchRequest.getNickname()),
                        board.category.categoryType.eq(CategoryType.GOD_LIFE_PAGE))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(board.createDate.desc())
                .fetch();

        Long count = queryFactory.select(board.count())
                .from(board)
                .where(keywordParam(boardSearchRequest.getKeyword()), tagsParam(boardSearchRequest.getTags()))
                .fetchOne();

        return new PageImpl<>(boards, pageable, count);
    }


    /**
     * 갓생 자극 페이지 상세 조회 메소드
     * @param boardId - 상세조회할 게시판 번호
     * @param loginMember - 현재 로그인한 유저 정보
     * @return 갓생 정보 상세 조회 DTO 반환
     */
    @Override
    public GodLifeStimulationBoardResponse findStimulusBoardEqualsBoardId(Long boardId, Member loginMember) {

        GodLifeStimulationBoardResponse godLifeStimulationBoardResponse = queryFactory.select(Projections.constructor(
                        GodLifeStimulationBoardResponse.class,
                        board.title.as("title"),
                        board.thumbnailUrl.as("thumbnailUrl"),
                        board.introduction.as("introduction"),
                        board.content.as("content"),
                        board.id.as("boardId"),
                        member.nickname.as("nickname"),
                        member.id.as("writerId"),
                        board.view.as("view"),
                        board.createDate,
                        board.totalScore.as("godLifeScore")
                ))
                .from(board)
                .join(member).on(board.member.eq(member))
                .join(category).on(board.category.categoryId.eq(category.categoryId))
                .where(board.id.eq(boardId), board.category.categoryType.eq(CategoryType.GOD_LIFE_STIMULUS),
                        board.status.eq(BoardStatus.S))
                .fetchOne();

        if (godLifeStimulationBoardResponse == null) {
            throw new NotFoundResource(ErrorMessage.INVALID_BOARD_MESSAGE.getErrorMessage());
        }

        queryFactory.update(board)
                .set(board.view, board.view.add(1))
                .where(board.id.eq(boardId)).execute();

        //게시판 주인 확인
        godLifeStimulationBoardResponse.setOwner(loginMember.getId().equals(godLifeStimulationBoardResponse.getWriterId()));

        return godLifeStimulationBoardResponse;
    }

    /**
     * @param pageable : 조회할 페이지 번호
     * @return 해당 페이지 번호에 포함되는 갓생 자극 게시판 간략 정보
     */
    @Override
    public Page<GodLifeStimulationBoardBriefResponse> findStimulusBoardPaging(Pageable pageable) {
        List<GodLifeStimulationBoardBriefResponse> content = queryFactory.select(Projections.fields(
                        GodLifeStimulationBoardBriefResponse.class,
                        board.title.as("title"),
                        board.thumbnailUrl.as("thumbnailUrl"),
                        board.introduction.as("introduction"),
                        board.id.as("boardId"),
                        board.member.nickname.as("nickname"),
                        board.totalScore.as("godLifeScore")
                ))
                .from(board)
                .join(board.member)
                .join(board.category)
                .where(board.category.categoryType.eq(CategoryType.GOD_LIFE_STIMULUS),
                        board.status.eq(BoardStatus.S))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(board.createDate.desc()) //내림 차순으로 정렬
                .fetch();

        Long count = queryFactory
                .select(board.count())
                .from(board)
                .join(board.category)
                .where(board.category.categoryType.eq(CategoryType.GOD_LIFE_STIMULUS), board.status.eq(BoardStatus.S))
                .fetchOne();

        return new PageImpl<>(content, pageable, count);
    }

    /**
     * @param request : 검색 조건
     * @return 검색 조건에 맞는 갓생 자극 페이지 전체 리스트 반환
     */
    @Override
    public List<GodLifeStimulationBoardBriefResponse> findStimulusBoardSearchCondition(StimulationBoardSearchCondition request) {
        List<GodLifeStimulationBoardBriefResponse> content = queryFactory.select(Projections.fields(
                        GodLifeStimulationBoardBriefResponse.class,
                        board.title.as("title"),
                        board.thumbnailUrl.as("thumbnailUrl"),
                        board.introduction.as("introduction"),
                        board.id.as("boardId"),
                        board.member.nickname.as("nickname"),
                        board.totalScore.as("godLifeScore")
                ))
                .from(board)
                .join(board.member)
                .join(board.category)
                .where(board.category.categoryType.eq(CategoryType.GOD_LIFE_STIMULUS),
                        board.status.eq(BoardStatus.S), nicknameParam(request.getNickname()),
                                doesTitleContainParam(request.getTitle()),
                                doesIntroductionContainParam(request.getIntroduction()))
                .orderBy(board.createDate.desc())
                .fetch();

        return content;
    }

    /**
     * 전체기간 가장 인기 있는 갓생 자극 게시물 조회
     * @return 가장 인기있는 순으로 갓생 자극 게시물 리스트 최대 10개 반환
     */
    @Override
    public List<GodLifeStimulationBoardBriefResponse> findAllTimePopularStimulusBoardList() {
        long start = System.currentTimeMillis();
        log.info("전체 기간 인기 있는 갓생 자극 게시물 조회 시작");
        List<GodLifeStimulationBoardBriefResponse> result = queryFactory
                .select(Projections.fields(
                        GodLifeStimulationBoardBriefResponse.class,
                        board.title.as("title"),
                        board.thumbnailUrl.as("thumbnail"),
                        board.introduction.as("introduction"),
                        board.id.as("boardId"),
                        board.member.nickname.as("nickname"),
                        board.totalScore.as("godLifeScore")))
                .from(board)
                .join(board.member)
                .join(board.category)
                .where(board.category.categoryType.eq(CategoryType.GOD_LIFE_STIMULUS),
                        board.status.eq(BoardStatus.S))
                .orderBy(board.totalScore.desc())
                .offset(0)
                .limit(10)
                .fetch();

        long end = System.currentTimeMillis();
        log.info("전체 기간 인기 있는 갓생 자극 게시물 조회 종료 시간 = {}", (end-start)/(double)1000);
        return result;
    }

    /**
     * 조회수가 가장 많은 갓생 자극 게시물을 조회합니다.
     */
    @Override
    public List<GodLifeStimulationBoardBriefResponse> findMostViewedBoardList(){
        return queryFactory.select(Projections.fields(
                GodLifeStimulationBoardBriefResponse.class,
                board.title.as("title"),
                board.thumbnailUrl.as("thumbnail"),
                board.introduction.as("introduction"),
                board.id.as("boardId"),
                board.member.nickname.as("nickname"),
                board.view.as("view")))
                .from(board)
                .join(board.member)
                .join(board.category)
                .where(board.category.categoryType.eq(CategoryType.GOD_LIFE_STIMULUS),
                        board.status.eq(BoardStatus.S))
                .orderBy(board.view.desc(), board.createDate.desc()) //조회수가 같으면 나중에 생성된 것부터
                .offset(0)
                .limit(10)
                .fetch();
    }


    private BooleanExpression nicknameParam(String nickname) {
        if(isBlankOrNullKeyword(nickname)) return null;

        return board.member.nickname.contains(nickname);
    }

    private BooleanExpression contentParm(String param) {
        if(isBlankOrNullKeyword(param)) return null;

        return board.content.contains(param);
    }

    private BooleanExpression doesTitleContainParam(String param) {
        if(isBlankOrNullKeyword(param)) return null;

        return board.title.contains(param);
    }

    private BooleanExpression keywordParam(String keyword) {
        if (isBlankOrNullKeyword(keyword)) return null;

        //return board.content.contains(keyword).or(board.title.contains(keyword));
        return contentParm(keyword).or(doesTitleContainParam(keyword));
    }

    private boolean isBlankOrNullKeyword(String keyword) {
        if(keyword == null || keyword.isBlank()){
            return true;
        }
        return false;
    }

    private BooleanBuilder tagsParam(String tags) {
        if (isBlankOrNullKeyword(tags)) {
            return null;
        }

        List<String> splitTag = Arrays.stream(tags.split(",")).toList();

        BooleanBuilder bb = new BooleanBuilder();

        for (String tag : splitTag) {
            bb.or(board.tag.contains(tag));
        }

        return bb;
    }

    private BooleanExpression doesIntroductionContainParam(String introduction) {
        if(isBlankOrNullKeyword(introduction)) return null;

        return board.introduction.contains(introduction);
    }


    private OrderSpecifier<?> boardSort(Pageable pageable) {
        if(pageable == null || pageable.getSort().isEmpty()) return null;

        Sort sort = pageable.getSort();
        for (Sort.Order order : sort) {
            Order direction = order.getDirection().isAscending() ? Order.ASC : Order.DESC;

            switch (order.getProperty()) {
                case "create_date" :
                    return new OrderSpecifier(direction, board.createDate);
            }
        }

        return null;
    }
}
