package com.god.life.service.alarm;


import com.god.life.domain.Alarm;
import com.god.life.domain.Board;
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
     * boardId에 대응되는 알람 저장
     */
    @Transactional
    public String saveAlarm(Long boardId, Long loginMemberId, String title, String content) {
        Board board = boardRepository.findMemberByBoardId(boardId); // 갓생 인정 이나 댓글을 다는 경우 게시판을 조회하는대 또 조회할 필요가 있을까?

        //토큰 값이 있고, 자신의 게시물이 아닌 경우
        String token = board.getMember().getFcmToken();

        // 추후에 알림 체크기능을 만들 것인지 물어봐야함
        if (!board.getMember().getId().equals(loginMemberId)) { // 게시물의 작성자가 로그인한 유저가 아닌 경우,
            Alarm alarm = Alarm.builder()
                    .boardId(boardId)
                    .memberId(board.getMember().getId())
                    .title(title)
                    .content(content)
                    .isRead(false)
                    .build();
            if (token != null) { // FCM 토큰이 있는 경우
                return token;
            }
            alarmRepository.save(alarm);
        }

        return null;
    }


    // 읽지 않은 알람을 가져옵니다. 없는 경우 빈 리스트
    @Transactional(readOnly = true)
    public List<Alarm> getUnreadAlarm(Long loginMemberId) {
        Optional<List<Alarm>> unreadAlarmByMemberId = alarmRepository.findUnreadAlarmByMemberId(loginMemberId);
        return unreadAlarmByMemberId.orElse(new ArrayList<>());
    }


}
