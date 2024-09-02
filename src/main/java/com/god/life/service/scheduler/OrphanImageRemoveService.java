package com.god.life.service.scheduler;


import com.god.life.domain.Image;
import com.god.life.service.ImageService;
import com.god.life.service.ImageUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


// 모종의 이유로 게시판이나 회원에 붙어있지 않은 사진을 제거하는 Service layer입니다.

@Service
@RequiredArgsConstructor
@Slf4j
public class OrphanImageRemoveService {

    private final ImageUploadService imageUploadService;
    private final ImageService imageService;

    // 매일 오전 3시에 삭제하도록
    @Scheduled(cron = "0 0 3 * * ?")
    //@Scheduled(fixedDelay = 50000)
    public void removeImages(){
        log.info("고아 이미지 삭제...");
        List<String> imageNamesInCloud = imageUploadService.getAllImageNames();
        List<String> imageNamesInDB = imageService.getAllImageNames();


        List<String> removeImageNames = new ArrayList<>();
        // 1. GCS 에 사진들을 저장후
        // 2. DB에 저장된 사진의 정보를 저장하므로
        // ==> GCS에 저장된 모든 파일이름 중 DB에 없는 파일 이름을 GCS에서 제거한다.
        for (String imageNameInCloud : imageNamesInCloud) {
            if (!imageNamesInDB.contains(imageNameInCloud)) {
                removeImageNames.add(imageNameInCloud);
            }
        }

        log.info("삭제 리스트 {} 삭제 성공", removeImageNames);
        // 삭제 처리 => 에러가 나도 다음날 다시 시도함.
        for (String removeImageName : removeImageNames) {
            imageUploadService.delete(removeImageName);
        }

        log.info("삭제 성공");
    }

}
