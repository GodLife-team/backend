package com.god.life.repository;

import com.god.life.domain.Member;
import com.god.life.dto.member.response.MemberInfoResponse;
import com.god.life.dto.popular.PopularMemberResponse;
import com.god.life.error.ErrorMessage;
import com.god.life.error.NotFoundResource;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static com.god.life.domain.QBoard.board;
import static com.god.life.domain.QGodLifeScore.godLifeScore;
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
        Optional<MemberInfoResponse> hasResponse = queryFactory
                .select(Projections.fields(
                        MemberInfoResponse.class,
                        member.nickname.as("nickname"),
                        member.whoAmI.as("whoAmI"),
                        member.profileName.as("profileImageURL"),
                        member.backgroundName.as("backgroundImageURL"),
                        Expressions.as(JPAExpressions //게시판 수 계산
                                .select(board.count().coalesce(0L))
                                .from(board)
                                .where(board.member.id.eq(findMemberId)), "memberBoardCount"),
                        Expressions.as(JPAExpressions.select(board.totalScore.sum().coalesce(0))
                                .from(board).where(board.member.id.eq(findMemberId)), "godLifeScore")
                ))
                .from(member)
                .where(member.id.eq(findMemberId))
                .stream().findFirst();

        if (hasResponse.isEmpty()) { //찾는 회원 정보가 없는 경우
            throw new NotFoundResource(ErrorMessage.INVALID_MEMBER_MESSAGE.getErrorMessage());
        }
        MemberInfoResponse response = hasResponse.get();

        return response;
    }

    @Override
    public List<PopularMemberResponse> findWeeklyPopularMember() {
        LocalDateTime today = LocalDateTime.now(); // 현재 시각
        //이번 주 월요일 0시 0분 0초
        LocalDateTime monday = LocalDateTime.of(LocalDate.now().with(DayOfWeek.MONDAY), LocalTime.MIDNIGHT);

        //인기 있는 회원 번호 조회 (받은 좋아요 수까지)
        List<PopularMemberResponse> weeklyPopularMembers = queryFactory.select(Projections.bean(
                        PopularMemberResponse.class,
                        member.id.as("memberId"),
                        godLifeScore.score.sum().as("godLifeScore")
                ))
                .from(member)
                .join(board).on(member.id.eq(board.member.id))
                .join(godLifeScore).on(board.id.eq(godLifeScore.board.id)) //점수 못받은 회원은 표시 X
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
                    weeklyPopularMember.setProfileURL(member.getProfileName() == null ? "" : member.getProfileName());
                    weeklyPopularMember.setBackgroundUrl(member.getBackgroundName() == null ? "" : member.getBackgroundName());
                }
            }
        }

        return weeklyPopularMembers;
    }

    // 전체 기간 인기있는 회원 반환
    @Override
    public List<PopularMemberResponse> findAllTimePopularMember() {
        //게시판 작성 개수에 따라 점수를 다르게 주면 되는 거 아닌가?
        //따라서 게시글 작성 개수를 가지고 갓생 점수 연산 ?
        //인기 있는 회원 번호 조회 (받은 좋아요 수까지)
        List<PopularMemberResponse> weeklyPopularMembers = queryFactory.select(Projections.bean(
                        PopularMemberResponse.class,
                        member.id.as("memberId"),
                        board.totalScore.sum().as("godLifeScore")
                ))
                .from(member)
                .join(board).on(member.id.eq(board.member.id))
                .groupBy(member.id)
                .orderBy(board.totalScore.sum().desc(), member.id.asc())
                .offset(0)
                .limit(10)
                .fetch(); // 탑 10명만 가져오기

        // 탑 10명의 멤버 정보 및 이미지 URL 조회하기
        List<Member> popularMember = queryFactory.selectFrom(member)
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
                    weeklyPopularMember.setProfileURL(member.getProfileName() == null ? "" : member.getProfileName());
                    weeklyPopularMember.setBackgroundUrl(member.getBackgroundName() == null ? "" : member.getBackgroundName());
                }
            }
        }


        return weeklyPopularMembers;
    }


}
