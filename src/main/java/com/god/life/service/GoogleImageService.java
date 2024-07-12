package com.god.life.service;


import com.god.life.dto.ImageSaveResponse;
import com.god.life.util.FileUtil;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Google Cloud Platform bucket에 이미지를 업로드하는 클래스
 */

@Service
@RequiredArgsConstructor
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

    // 여러 사진 업로드
    @Override
    public List<ImageSaveResponse> uploads(List<MultipartFile> Images) {
        List<ImageSaveResponse> responses = new ArrayList<>();

        for (MultipartFile image : Images) {
            try {
                responses.add(upload(image));
            } catch (IOException ex) {
                for (int i = 0; i < responses.size(); i++) { //사진 업로드에 실패했으면 이전까지 업로드했던 사진 삭제처리
                    delete(responses.get(i).getServerName());
                }
                throw new IllegalStateException("사진 업로드에 실패했습니다. 다시 시도해 주세요.");
            }
        }

        return responses;
    }

    // 사진 삭제
    @Override
    public void delete(String fileName) {
        BlobId blobId = BlobId.of(bucketName, fileName);
        storage.delete(blobId);
    }


    @Override
    public List<String> getAllImageNames() {
        List<String> saveImageNameInCloud = new ArrayList<>();
        storage.list(bucketName).getValues()
                .forEach(blob -> saveImageNameInCloud.add(blob.getName()));
        return saveImageNameInCloud;
    }

}
