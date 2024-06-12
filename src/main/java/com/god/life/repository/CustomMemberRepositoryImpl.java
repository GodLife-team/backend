package com.god.life.repository;

import com.god.life.dto.MemberInfoResponse;
import com.god.life.error.ErrorMessage;
import com.god.life.error.NotFoundResource;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.god.life.domain.QBoard.board;
import static com.god.life.domain.QGodLifeScore.godLifeScore;
import static com.god.life.domain.QImage.image;
import static com.god.life.domain.QMember.member;

@Slf4j
@Repository
public class CustomMemberRepositoryImpl implements CustomMemberRepository {

    private final JPAQueryFactory queryFactory;

    public CustomMemberRepositoryImpl(EntityManager em){
        queryFactory = new JPAQueryFactory(em);

    }

    // findMEmberId로 조회하려는 유저의 정보를 조회함,
    @Override
    public MemberInfoResponse getMemberInfo(Long findMemberId) {

        // 해당 회원의 닉네임, 자기소개, 작성한 게시물 수 계산
        List<MemberInfoResponse> responses = queryFactory
                .select(Projections.bean(
                        MemberInfoResponse.class,
                        member.nickname.as("nickname"),
                        member.whoAmI.as("whoAmI"),
                        board.count().as("memberBoardCount")))
                .from(member)
                .leftJoin(board)
                .on(member.id.eq(board.member.id))
                .where(member.id.eq(findMemberId))
                .groupBy(member.id)
                .fetch();

        if (responses.size() == 0) { //조인 결과가 없는 경우 --> 회원  XXX
            throw new NotFoundResource(ErrorMessage.INVALID_MEMBER_MESSAGE.getErrorMessage());
        }

        MemberInfoResponse response = responses.get(0);

        // 이미지 조회
        List<String> images =
                queryFactory.select(image.serverName)
                        .from(image)
                        .where(image.member.id.eq(findMemberId))
                        .fetch();
        for (String image : images) {
            if (image.startsWith("profile")) {
                response.setProfileImageURL(image.substring("profile".length()));
            } else if (image.startsWith("background")) {
                response.setBackgroundImageURL(image.substring("background".length()));
            }
        }

        // 받은 갓생 점수 계산
        List<Integer> totalGodLife = queryFactory.select(godLifeScore.score.sum())
                .from(board)
                .leftJoin(godLifeScore)
                .on(board.id.eq(godLifeScore.board.id))
                .where(board.member.id.eq(findMemberId))
                .groupBy(board.member.id)
                .fetch();

        if (totalGodLife.size() == 0) {
            response.setGodLifeScore(0);
        }else{
            response.setGodLifeScore(totalGodLife.get(0));
        }

        return response;
    }
}
