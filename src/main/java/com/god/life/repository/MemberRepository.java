package com.god.life.repository;

import com.god.life.domain.Member;
import com.god.life.dto.MemberInfoResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long>, CustomMemberRepository {



    Optional<Member> findByRefreshToken(String refreshToken);

    boolean existsByNickname(String nickname);

    boolean existsByEmail(String email);

    boolean existsByProviderId(String id);

    @Query("SELECT m from Member m left join fetch m.images where m.id = :memberId")
    Optional<Member> findByIdWithImage(@Param("memberId") Long memberId);

    Optional<Member> findByProviderId(String providerId);


//    @Query(value = "select new com.god.life.dto.MemberInfoResponse(m.nickname, m.whoAmI, coalesce(count(b.board_count), 0), coalesce(count(g.total_like), 0))" +
//            " from Member m " +
//            " left join (select b.member_id, count(*) as board_count from board b group by b.member_id) b on m.member_id = b.member_id " +
//            " left join (select b.member_id, count(*) as total_likes from board b join god_life_score g on b.board_id = g.board_id group by b.member_id) g on m.member_id = g.member_id " +
//            " where m.member_id = :memberId")

    @Query("SELECT new com.god.life.dto.MemberInfoResponse(m.nickname, m.whoAmI, " +
            "coalesce((SELECT COUNT(b) FROM Board b WHERE b.member.id = m.id), 0), " +
            "coalesce((SELECT sum(g.score) FROM GodLifeScore g WHERE g.board.member.id = m.id), 0)) " +
            "FROM Member m WHERE m.id = :memberId")
    Optional<MemberInfoResponse> getMemberTotalInfo(@Param("memberId") Long findMemberId);


    //@Query("SELECT from Member m left join fetch m.images where m.id = :memberId")
//    SELECT m.member_id, m.nickname, m.introduction,
//    COALESCE(b.board_count, 0) AS board_count,
//    COALESCE(g.total_likes, 0) AS total_likes
//    FROM member m
//    LEFT JOIN (
//            SELECT b.member_id, COUNT(*) AS board_count
//    FROM board b
//    GROUP BY b.member_id
//) b ON m.member_id = b.member_id
//    LEFT JOIN (
//    SELECT b.member_id, COUNT(*) AS total_likes
//    FROM board b
//    join god_life_score g ON b.board_id = g.board_id
//    GROUP BY b.member_id
//) g ON m.member_id = g.member_id WHERE m.member_id = 7

}
