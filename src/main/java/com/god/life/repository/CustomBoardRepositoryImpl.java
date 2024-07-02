package com.god.life.repository;

import com.god.life.domain.*;
import com.god.life.dto.*;
import com.god.life.error.ErrorMessage;
import com.god.life.error.NotFoundResource;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.*;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.god.life.domain.QBoard.board;
import static com.god.life.domain.QCategory.category;
import static com.god.life.domain.QGodLifeScore.godLifeScore;
import static com.god.life.domain.QMember.member;

@Repository
@Slf4j
public class CustomBoardRepositoryImpl implements CustomBoardRepository {

    private final JPAQueryFactory queryFactory;
    private static final DateTimeFormatter timeFomatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public CustomBoardRepositoryImpl(EntityManager em){
        queryFactory = new JPAQueryFactory(em);
    }

    // 1주간 인기 게시글 조회
    // message에 시간 붙여주기 --> 아직 미구현 XXX
    // 6/26 : 갓생 인증 게시글만 보여줄 것인지?
    @Override
    public List<BoardSearchResponse> findWeeklyPopularBoard() {
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

        // In 절 쿼리 -> 1:N 여러번을 할 수 없으므로
        boards.stream().map(Board::getComments).forEach(Hibernate::initialize); // 댓글 fetch 조인
        boards.stream().map(Board::getImages).forEach(Hibernate::initialize); // 이미지 fetch 조인
        boards.forEach(b -> b.getMember().getImages().forEach(Hibernate::initialize));

        //조립
        List<BoardSearchResponse> result = new ArrayList<>();
        for (int i = 0; i < boards.size(); i++) {
            BoardSearchResponse dto = BoardSearchResponse.of(boards.get(i), false);
            dto.setGodScore(weeklyPopularBoardDTO.get(i).getSum());
            result.add(dto);
        }

        return result;
    }

    // 전체 인기 있는 게시물 조회
    @Override
    public List<BoardSearchResponse> findTotalPopularBoard() {

        // 전체 기간 인기 있는 게시물 조회
        List<PopularBoardQueryDTO> mostPopularBoardDTO = queryFactory.select(Projections.bean(
                        PopularBoardQueryDTO.class,
                        board.id.as("boardId"),
                        godLifeScore.score.sum().as("sum")
                ))
                .from(board)
                .join(category).on(board.category.categoryId.eq(category.categoryId))
                .join(godLifeScore).on(board.id.eq(godLifeScore.board.id))
                .where(category.categoryType.eq(CategoryType.GOD_LIFE_PAGE))
                .groupBy(board.id)
                .orderBy(godLifeScore.score.sum().desc(), board.id.asc())
                .offset(0)
                .limit(10) // 10개만 fetch
                .fetch();

        //인기 있는 게시판 정보 조회
        List<Board> boards = queryFactory.selectFrom(board)
                .join(board.member, member).fetchJoin()
                .where(
                        board.id.in(mostPopularBoardDTO.stream()
                                .map(PopularBoardQueryDTO::getBoardId).collect(Collectors.toList())))
                .orderBy(board.id.asc())
                .fetch();

        // In 절 쿼리 -> 1:N 여러번을 할 수 없으므로
        boards.stream().map(Board::getComments).forEach(Hibernate::initialize); // 댓글 fetch 조인
        boards.stream().map(Board::getImages).forEach(Hibernate::initialize); // 이미지 fetch 조인
        boards.forEach(b -> b.getMember().getImages().forEach(Hibernate::initialize));

        //조립
        List<BoardSearchResponse> result = new ArrayList<>();
        for (int i = 0; i < boards.size(); i++) {
            BoardSearchResponse dto = BoardSearchResponse.of(boards.get(i), false);
            dto.setGodScore(mostPopularBoardDTO.get(i).getSum());
            result.add(dto);
        }

        return result;
    }

    @Override
    public GodLifeStimulationBoardResponse findStimulusBoardEqualsBoardId(Long boardId, Member loginMember) {

        GodLifeStimulationBoardResponse godLifeStimulationBoardResponse = queryFactory.select(Projections.fields(
                        GodLifeStimulationBoardResponse.class,
                        board.title.as("title"),
                        board.thumbnailUrl.as("thumbnailUrl"),
                        board.introduction.as("introduction"),
                        board.content.as("content"),
                        board.id.as("boardId"),
                        member.nickname.as("nickname"),
                        member.id.as("writerId"),
                        ExpressionUtils.as // 해당 갓생 자극 점수 추출
                                (JPAExpressions.select(godLifeScore.score.sum().coalesce(0)).from(godLifeScore).where(godLifeScore.board.eq(board)),
                                        "godLifeScore")
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

        //게시판 주인 확인
        godLifeStimulationBoardResponse.setOwner(loginMember.getId().equals(godLifeStimulationBoardResponse.getWriterId()));

        return godLifeStimulationBoardResponse;
    }

    @Override
    public Page<GodLifeStimulationBoardResponse> findStimulusBoardPaging(Pageable pageable) {
        List<GodLifeStimulationBoardResponse> content = queryFactory.select(Projections.fields(
                        GodLifeStimulationBoardResponse.class,
                        board.title.as("title"),
                        board.thumbnailUrl.as("thumbnailUrl"),
                        board.introduction.as("introduction"),
                        board.id.as("boardId"),
                        board.member.nickname.as("nickname"),
                        board.member.id.as("writerId"),
                        board.content.as("content"),
                        ExpressionUtils.as // 해당 갓생 자극 점수 추출
                                (JPAExpressions.select(godLifeScore.score.sum().coalesce(0)).from(godLifeScore).where(godLifeScore.board.eq(board)),
                                        "godLifeScore")
                ))
                .from(board)
                .join(board.member)
                .join(board.category)
                .where(board.category.categoryType.eq(CategoryType.GOD_LIFE_STIMULUS),
                        board.status.eq(BoardStatus.S))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(board.createDate.desc())
                .fetch();

        Long count = queryFactory
                .select(board.count())
                .from(board)
                .join(board.category)
                .where(board.category.categoryType.eq(CategoryType.GOD_LIFE_STIMULUS))
                .fetchOne();

        return new PageImpl<>(content, pageable, count);
    }

    //검색 조건에 맞는 갓생 자극 페이지 조회
    @Override
    public List<GodLifeStimulationBoardResponse> findStimulusBoardSearchCondition(GodStimulationBoardSearchRequest request) {
        List<GodLifeStimulationBoardResponse> content = queryFactory.select(Projections.fields(
                        GodLifeStimulationBoardResponse.class,
                        board.title.as("title"),
                        board.thumbnailUrl.as("thumbnailUrl"),
                        board.introduction.as("introduction"),
                        board.id.as("boardId"),
                        board.member.nickname.as("nickname"),
                        board.member.id.as("writerId"),
                        board.content.as("content"),
                        ExpressionUtils.as // 해당 갓생 자극 점수 추출
                                (JPAExpressions.select(godLifeScore.score.sum().coalesce(0)).from(godLifeScore).where(godLifeScore.board.eq(board)),
                                        "godLifeScore")
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

    private BooleanExpression doesIntroductionContainParam(String introduction) {
        if(isBlankOrNullKeyword(introduction)) return null;

        return board.introduction.contains(introduction);
    }

    // 조건 검색에 맞는 갓생글 조회
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

    private BooleanExpression nicknameParam(String nickname) {
        if(isBlankOrNullKeyword(nickname)) return null;

        return board.member.nickname.eq(nickname);
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
