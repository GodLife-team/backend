package com.god.life.repository;

import com.god.life.domain.Image;
import com.god.life.domain.Member;
import com.god.life.domain.QMember;
import com.god.life.dto.MemberInfoResponse;
import com.god.life.dto.PopularBoardQueryDTO;
import com.god.life.dto.PopularMemberResponse;
import com.god.life.error.ErrorMessage;
import com.god.life.error.NotFoundResource;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

    // findMemberId로 조회하려는 유저의 정보를 조회함,
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

        if (totalGodLife.size() == 0 || totalGodLife.get(0) == null) {
            response.setGodLifeScore(0);
        }else{
            response.setGodLifeScore(totalGodLife.get(0));
        }

        return response;
    }

    @Override
    public List<PopularMemberResponse> findWeeklyPopularMember() {
        LocalDateTime today = LocalDateTime.now(); // 현재 시각
        //이번 주 월요일 0시 0분 0초
        LocalDateTime monday = LocalDateTime.of(LocalDate.now().with(DayOfWeek.MONDAY), LocalTime.MIDNIGHT);

        //인기 있는 게시물 번호 조회 (좋아요 수까지)
        List<PopularMemberResponse> weeklyPopularMembers = queryFactory.select(Projections.bean(
                        PopularMemberResponse.class,
                        member.id.as("memberId"),
                        godLifeScore.score.sum().as("godLifeScore")
                ))
                .from(member)
                .join(board).on(member.id.eq(board.member.id))
                .join(godLifeScore).on(board.id.eq(godLifeScore.board.id))
                .where(
                        godLifeScore.createDate.between(monday, today) //일주일 간격으로 수행
                )
                .groupBy(member.id)
                .orderBy(godLifeScore.score.sum().desc(), member.id.asc())
                .offset(0)
                .limit(10)
                .fetch(); // 탑 10명만 가져오기

        // 탑 10명의 멤버 정보 및 이미지 URL 조회하기
        List<Member> popularMember = queryFactory.selectFrom(member)
                .leftJoin(member.images)
                .fetchJoin()
                .where(member.id.in(
                        weeklyPopularMembers.stream().map(PopularMemberResponse::getMemberId).toList()))
                .fetch();


        // 조립
        for (int i = 0; i < weeklyPopularMembers.size(); i++) {
            var weeklyPopularMember = weeklyPopularMembers.get(i);
            for (int j = 0; j < popularMember.size(); j++) {
                Member member = popularMember.get(j);
                if (weeklyPopularMember.getMemberId().equals(member.getId())) {
                    weeklyPopularMember.setNickname(member.getNickname());
                    weeklyPopularMember.setWhoAmI(member.getWhoAmI());
                    List<Image> images = member.getImages();
                    for (Image image : images) {
                        if (image.getServerName().contains("profile")) {
                            weeklyPopularMember.setProfileURL(image.getServerName().substring("profile".length()));
                        }
                    }
                    break;
                }
            }
        }

        return weeklyPopularMembers;
    }


}
