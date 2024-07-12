package com.god.life.service;


import com.god.life.domain.Board;
import com.god.life.domain.GodLifeScore;
import com.god.life.domain.Member;
import com.god.life.error.BadRequestException;
import com.god.life.error.ErrorMessage;
import com.god.life.error.NotFoundResource;
import com.god.life.error.UniqueException;
import com.god.life.repository.BoardRepository;
import com.god.life.repository.GodLifeScoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class GodLifeScoreService {

    private final BoardRepository boardRepository;
    private final GodLifeScoreRepository godLifeScoreRepository;

    //갓생 인정을 하는 메소드입니다.
    @Transactional
    public Boolean likeBoard(Member member, Long boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new NotFoundResource(ErrorMessage.INVALID_BOARD_MESSAGE.getErrorMessage()));

        //이미 좋아요를 누른 경우
        boolean alreadyAgreed = godLifeScoreRepository.existsByBoardAndMember(board, member);
        if (alreadyAgreed) {
            throw new UniqueException(ErrorMessage.ALREADY_MARKED_MESSAGE.getErrorMessage());
        }

        GodLifeScore godLifeScore = GodLifeScore.likeMemberToBoard(member, board);
        godLifeScoreRepository.save(godLifeScore);
        boardRepository.incrementGodLifeScore(boardId);
        return true;
    }

    /**
     * @param board
     * @param member
     * @return 이미 갓생 인정한 기록이 있는지
     */
    public boolean isMemberLikedBoard(Board board, Member member) {
        return godLifeScoreRepository.existsByBoardAndMember(board, member);
    }


    /**
     * @param boardId 갓생 인정을 취소할 ID
     * @param member 갓생 인정을 취소할 회원
     */
    @Transactional
    public void cancelLike(Long boardId, Member member) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new NotFoundResource("존재하지 않는 게시판입니다."));

        godLifeScoreRepository.deleteByBoardAndMember(board, member);
        boardRepository.decrementGodLifeScore(boardId);
    }

    /**
     * @param boardId : 갓생 점수를 계산할 게시판 번호
     * @return boardId가 받은 갓생 점수를 반환합니다.
     */
    public int calculateGodLifeScoreBoard(Long boardId) {
        Integer boardScore = godLifeScoreRepository.calculateGodLifeScoreWithBoardId(boardId);
        return boardScore == null ? 0 : boardScore;
    }


    /** 해당 회원이 갓생 인정 기록을 모두 삭제합니다.
     * @param deleteMember : 탈퇴하는 회원
     */
    @Transactional
    public void deleteUserLikedHistory(Member deleteMember) {
        godLifeScoreRepository.deleteByMember(deleteMember);
    }

}
