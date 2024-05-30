package com.god.life.repository;

import com.god.life.domain.Board;
import com.god.life.domain.QBoard;
import com.god.life.domain.QMember;
import com.god.life.dto.BoardSearchRequest;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;

import static com.god.life.domain.QBoard.board;

@Repository
public class CustomBoardRepositoryImpl implements CustomBoardRepository {

    private final JPAQueryFactory queryFactory;

    public CustomBoardRepositoryImpl(EntityManager em){
        queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Page<Board> findBoardWithSearchRequest(BoardSearchRequest boardSearchRequest, Pageable pageable) {
        List<Board> boards = queryFactory.selectFrom(board)
                .join(QBoard.board.member, QMember.member).fetchJoin()
                .where(keywordParam(boardSearchRequest.getKeyword()),tagsParam(boardSearchRequest.getTags()))
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
