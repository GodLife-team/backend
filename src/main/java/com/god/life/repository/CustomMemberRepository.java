package com.god.life.repository;

import com.god.life.dto.MemberInfoResponse;
import com.god.life.dto.PopularMemberResponse;

import java.util.List;

public interface CustomMemberRepository {


    MemberInfoResponse getMemberInfo(Long findMemberId);

    // 한 주간 인기 있는 회원 정보 반환
    List<PopularMemberResponse> findWeeklyPopularMember();


    // 전체 기간 인기 있는 회원 정보 반환
    List<PopularMemberResponse> findAllTimePopularMember();

}
