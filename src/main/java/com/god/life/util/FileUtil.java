package com.god.life.util;

import com.god.life.error.BadRequestException;
import com.god.life.error.ForbiddenException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Slf4j
public class FileUtil {

    private static final String INVALID_EXT = "업로드할 수 없는 파일 유형입니다.";

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
        if (!VALID_IMAGE_EXT.contains(ext.toLowerCase())) { //추가할 수 있는 확장자가 아니라면
            throw new ForbiddenException(INVALID_EXT);
        }

        return ext;
    }

    /**
     * @param imageFile
     * 해당 파일이 image 형식인지 확인합니다.
     */
    public static void validateFileExt(MultipartFile imageFile) {
        String contentType = imageFile.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BadRequestException(INVALID_EXT);
        }
    }
}
