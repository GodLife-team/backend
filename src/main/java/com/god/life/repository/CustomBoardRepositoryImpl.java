package com.god.life.repository;

import com.god.life.domain.Board;
import com.god.life.domain.QBoard;
import com.god.life.domain.QGodLifeScore;
import com.god.life.domain.QMember;
import com.god.life.dto.BoardResponse;
import com.god.life.dto.BoardSearchRequest;
import com.god.life.dto.BoardSearchResponse;
import com.god.life.dto.PopularBoardQueryDTO;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Ops;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.god.life.domain.QBoard.board;
import static com.god.life.domain.QGodLifeScore.godLifeScore;

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
                .join(godLifeScore).on(board.id.eq(godLifeScore.board.id))
                .where(
                        godLifeScore.createDate.between(monday, today) //일주일 간격으로 수행
                )
                .groupBy(board.id)
                .orderBy(godLifeScore.score.sum().desc(), board.id.asc())
                .offset(0)
                .limit(10) // 10개만 fetch
                .fetch();

        //인기 있는 게시판 정보 조회
        List<Board> boards = queryFactory.selectFrom(board)
                .join(board.member, QMember.member).fetchJoin()
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
                .join(godLifeScore).on(board.id.eq(godLifeScore.board.id))
                .groupBy(board.id)
                .orderBy(godLifeScore.score.sum().desc(), board.id.asc())
                .offset(0)
                .limit(10) // 10개만 fetch
                .fetch();

        //인기 있는 게시판 정보 조회
        List<Board> boards = queryFactory.selectFrom(board)
                .join(board.member, QMember.member).fetchJoin()
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

    // 조건 검색에 맞는 갓생글 조회
    @Override
    public Page<Board> findBoardWithSearchRequest(BoardSearchRequest boardSearchRequest, Pageable pageable) {
        List<Board> boards = queryFactory.selectFrom(board)
                .join(QBoard.board.member, QMember.member).fetchJoin()
                .where(keywordParam(boardSearchRequest.getKeyword()),tagsParam(boardSearchRequest.getTags()),
                        nicknameParam(boardSearchRequest.getNickname()))
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

    private BooleanExpression keywordParam(String keyword) {
        if (isBlankOrNullKeyword(keyword)) return null;

        return board.content.contains(keyword).or(board.title.contains(keyword));
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

}
