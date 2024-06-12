package com.god.life.repository;

import com.god.life.domain.Member;
import com.god.life.dto.MemberInfoResponse;

public interface CustomMemberRepository {

    MemberInfoResponse getMemberInfo(Long findMemberId);


}
