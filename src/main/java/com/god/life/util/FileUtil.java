package com.god.life.util;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class FileUtil {

    private static final List<String> VALID_IMAGE_EXT = List.of("jpg, png");

    public static String createServerName(){ //서버 이미지 이름 생성
        return UUID.randomUUID().toString();
    }

    public static String getExt(String fileName){
        //확장자 추출
        int extIdx = fileName.lastIndexOf('.');
        if(extIdx == -1) {
            throw new IllegalArgumentException("잘못된 확장자입니다.");
        }

        // 파일 종류 확인
        String ext = fileName.substring(extIdx);
        if (!VALID_IMAGE_EXT.contains(ext.toLowerCase())) { //추가할수 있는 확장자가 아니라면
            throw new IllegalArgumentException("저장할 수 없는 확장자입니다.");
        }

        return ext;
    }

}
