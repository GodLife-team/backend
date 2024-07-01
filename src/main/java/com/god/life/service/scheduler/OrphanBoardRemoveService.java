package com.god.life.service.scheduler;


import com.god.life.service.BoardService;
import com.god.life.service.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class OrphanBoardRemoveService {

    private final BoardService boardService;

    public OrphanBoardRemoveService(BoardService boardService) {
        this.boardService = boardService;
    }

    // 스케쥴러가 실행되는 동안에 딱 임시 게시글을 확인하는 경우..???
    // createDATE -> 하루 이전에 생성된 임시 게시묾만 제거
    // 매일 오전 2시에 삭제하도록
    @Scheduled(cron = "0 0 2 * * ?")
    public void removeOrphanBoard(){
        log.info("미완료된 게시판 및 이미지 삭제");
        List<Long> incompleteWriteBoardIds =
                boardService.getIncompleteWriteBoardIds(LocalDateTime.now().minusDays(1));
        log.info("삭제 완료 미완료된 게시판 아이디.... {}", incompleteWriteBoardIds);
    }


}
