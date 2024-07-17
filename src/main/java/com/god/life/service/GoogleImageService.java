package com.god.life.service;


import com.god.life.dto.image.ImageSaveResponse;
import com.god.life.error.BadRequestException;
import com.god.life.error.InternalServerException;
import com.god.life.util.FileUtil;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Google Cloud Platform bucket에 이미지를 업로드하는 클래스
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleImageService implements ImageUploadService{

    private static final String TOO_MANY_FILE = "사진 수가 너무 많습니다.";
    private static final String FAILURE_FILE_UPLOAD = "사진 업로드에 실패했습니다. 다시 시도해 주세요.";

    @Value("${spring.cloud.gcp.storage.bucket}")
    private String bucketName;

    private final Storage storage;

    private final Executor executor;

    // GCP Bucket에 요청한 이미지 저장
    @Override
    public ImageSaveResponse upload(MultipartFile file) {
        FileUtil.validateFileExt(file);
        String originName = file.getOriginalFilename();
        String serverName = FileUtil.createServerName(file.getOriginalFilename());

        // 이미지 업로드
        try {
            storage.createFrom(BlobInfo.newBuilder(bucketName, serverName)
                            .setContentType(file.getContentType()).build()
                    , file.getInputStream());
        } catch (IOException | StorageException ex) {
            log.error("GCP에 업로드 실패", ex);
            throw new InternalServerException("GCP에 이미지 업로드 실패..");
        }

        return new ImageSaveResponse(originName, serverName);
    }


    //여러 사진 업로드
    @Override
    public List<ImageSaveResponse> uploads(List<MultipartFile> images) {
        if (MAX_IMAGE_LENGTH < images.size()) {
            throw new BadRequestException(TOO_MANY_FILE);
        }

        List<ImageSaveResponse> responses = new CopyOnWriteArrayList<>();
        List<CompletableFuture<ImageSaveResponse>> futures = images.stream()
                .map(image -> CompletableFuture.supplyAsync(() -> {
                    long start = System.currentTimeMillis();
                    ImageSaveResponse response = upload(image);
                    log.info("업로드 수행 시간 = {}ms", System.currentTimeMillis() - start);
                    return response;
                }, executor)).toList();

        AtomicBoolean failureChecker = new AtomicBoolean(false);

        futures.forEach(future -> {
            try {
                responses.add(future.join());
            } catch (CancellationException | CompletionException ex) {
                failureChecker.set(true);
            }
        });

        if (failureChecker.get()) {
            for (ImageSaveResponse response : responses) {
                executor.execute(() -> delete(response.getServerName()));
            }
            throw new InternalServerException("이미지 업로드 중 에러가 발생했습니다. 다시 시도해 주세요");
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
