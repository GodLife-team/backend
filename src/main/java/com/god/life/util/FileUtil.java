package com.god.life.util;

import com.god.life.error.ForbiddenException;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

@Slf4j
public class FileUtil {

    private static final String INVALID_EXT = "잘못된 확장자입니다.";

    private static final List<String> VALID_IMAGE_EXT = List.of(".jpg", ".png", ".jpeg");

    public static String createServerName(){ //서버 이미지 이름 생성
        return UUID.randomUUID().toString();
    }

    /**
     * @param fileName 저장할 파일 이름
     * @return 해당 파일의 확장자를 반환합니다.
     */
    public static String getExt(String fileName){
        //확장자 추출
        int extIdx = fileName.lastIndexOf('.');
        if(extIdx == -1) {
            throw new ForbiddenException(INVALID_EXT);
        }

        // 파일 종류 확인
        String ext = fileName.substring(extIdx);
        log.info("ext : {}", ext.toLowerCase());
        if (!VALID_IMAGE_EXT.contains(ext.toLowerCase())) { //추가할수 있는 확장자가 아니라면
            throw new ForbiddenException(INVALID_EXT);
        }

        return ext;
    }

}
