package com.god.life.service;


import com.god.life.domain.Board;
import com.god.life.domain.GodLifeScore;
import com.god.life.domain.Member;
import com.god.life.error.NotFoundResource;
import com.god.life.repository.BoardRepository;
import com.god.life.repository.GodLifeScoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
//@Transactional
public class GodLifeScoreService {

    private final BoardRepository boardRepository;
    private final GodLifeScoreRepository godLifeScoreRepository;

    //@Transactional --> Transaction silently rolled back because it has been marked as rollback-only
    public Boolean likeBoard(Member member, Long boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new NotFoundResource("존재하지 않는 게시판입니다."));

        GodLifeScore godLifeScore = GodLifeScore.likeMemberToBoard(member, board);

        try {
            godLifeScoreRepository.save(godLifeScore);
        } catch (DataIntegrityViolationException ex) {
            return false;
        }

        return true;
    }

    @Transactional(readOnly = true)
    public boolean isMemberLikedBoard(Board board, Member member) {
        return godLifeScoreRepository.existsByBoardAndMember(board, member);
    }


    @Transactional
    public void cancelLike(Long boardId, Member member) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new NotFoundResource("존재하지 않는 게시판입니다."));

        godLifeScoreRepository.deleteByBoardAndMember(board, member);
    }

    @Transactional(readOnly = true)
    public int calculateGodLifeScoreBoard(Long boardId) {
        Integer boardScore = godLifeScoreRepository.calculateGodLifeScoreWithBoardId(boardId);
        return boardScore == null ? 0 : boardScore;
    }

    public Integer calculateGodLifeScoreMember(Member loginMember) {
        Integer memberScore = godLifeScoreRepository.calculateGodLifeScoreWithMember(loginMember.getId());
        return memberScore == null ? 0 : memberScore;
    }
}
