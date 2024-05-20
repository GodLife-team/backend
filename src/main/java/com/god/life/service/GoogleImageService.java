package com.god.life.service;


import com.god.life.dto.ImageSaveResponse;
import com.god.life.util.FileUtil;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GoogleImageService implements ImageUploadService {

    @Value("${spring.cloud.gcp.storage.bucket}")
    private String bucketName;
    private final Storage storage;


    // GCP Bucket에 요청한 이미지 저장
    @Override
    public ImageSaveResponse upload(MultipartFile file) throws IOException {
        String serverName = FileUtil.createServerName();
        String originName = file.getOriginalFilename();
        String ext = FileUtil.getExt(originName);

        // 이미지 업로드
        storage.createFrom(BlobInfo.newBuilder(bucketName, serverName).setContentType(ext).build()
        , file.getInputStream());

        return new ImageSaveResponse(originName, serverName);
    }
}
