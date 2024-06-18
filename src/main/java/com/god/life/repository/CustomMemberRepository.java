package com.god.life.repository;

import com.god.life.domain.Member;
import com.god.life.dto.MemberInfoResponse;
import com.god.life.dto.PopularMemberResponse;

import java.util.List;

public interface CustomMemberRepository {

    MemberInfoResponse getMemberInfo(Long findMemberId);

    List<PopularMemberResponse> findWeeklyPopularMember();

}
