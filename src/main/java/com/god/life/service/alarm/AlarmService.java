package com.god.life.service.alarm;


import com.god.life.domain.Alarm;
import com.god.life.domain.Board;
import com.god.life.domain.Member;
import com.god.life.dto.board.BoardAlarmInfo;
import com.god.life.error.NotFoundResource;
import com.god.life.repository.AlarmRepository;
import com.god.life.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AlarmService {

    private final AlarmRepository alarmRepository;
    private final BoardRepository boardRepository;

    /**
     * boardId에 대응되는 알람 저장 ==> boardId 에 대응되는 게시판을 작성한 게시판 작성자 정보 반환
     */
    @Transactional
    public Member saveAlarm(Long loginMemberId, BoardAlarmInfo info) {
        Board board = boardRepository.findMemberByBoardId(info.getBoardId());
        Member boardOwner = board.getMember();

        if (!boardOwner.getId().equals(loginMemberId)) { // 게시물의 작성자가 로그인한 유저가 아닌 경우,
            Alarm alarm = Alarm.builder()
                    .boardId(info.getBoardId())
                    .memberId(boardOwner.getId())
                    .title(info.getTitle())
                    .content(info.getContent())
                    .categoryType(info.getCategoryType())
                    .isRead(false)
                    .build();
            alarmRepository.save(alarm);
        }

        return boardOwner;
    }


    // 읽지 않은 알람을 가져옵니다. 없는 경우 빈 리스트
    @Transactional(readOnly = true)
    public List<Alarm> getAlarms(Long loginMemberId) {
        Optional<List<Alarm>> alarmByMemberId = alarmRepository.findAlarmByMemberId(loginMemberId);
        return alarmByMemberId.orElse(new ArrayList<>());
    }


    @Transactional
    public void updateReadAlarm(Long alarmId, Long memberId) {
        Alarm alarm = alarmRepository.findAlarmByAlarmIdAndMemberId(alarmId, memberId)
                .orElseThrow(() -> new NotFoundResource("잘못된 알람 읽기 업데이트 요청입니다."));

        alarm.checkRead(); //읽음 처리
    }
}
