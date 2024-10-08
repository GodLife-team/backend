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

/**
 * Google Cloud Platform bucket에 이미지를 업로드하는 클래스
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleImageService implements ImageUploadService{

    private static final String FAILURE_FILE_UPLOAD = "사진 업로드에 실패했습니다. 다시 시도해 주세요.";

    @Value("${spring.cloud.gcp.storage.bucket}")
    private String bucketName;

    private final Storage storage;

    private final Executor executor;

    // GCP Bucket에 요청한 이미지 저장
    @Override
    public ImageSaveResponse upload(MultipartFile file) {
        FileUtil.validateFileExt(file);
        return uploadInternal(file);
    }

    //여러 사진 업로드
    @Override
    public List<ImageSaveResponse> uploads(List<MultipartFile> images) {
        validateImage(images);

        List<CompletableFuture<ImageSaveResponse>> futures = images.stream()
                .map(image -> CompletableFuture.supplyAsync(() -> uploadInternal(image), executor)
                        .exceptionally(ex -> null)) //실패시 null 로 반환하도록 만듬
                .toList();

        List<ImageSaveResponse> containFailure =
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                        .thenApply(Void -> futures.stream()
                                .map(CompletableFuture::join)
                                .toList())
                        .join();

        // null 제외
        List<ImageSaveResponse> successImages = containFailure.stream().filter(Objects::nonNull).toList();
        // 업로드 실패시 성공한 이미지 정보 삭제처리 진행
        if (successImages.size() < images.size()) {
            for (ImageSaveResponse response : successImages) {
                executor.execute(() -> delete(response.getServerName()));
            }
            throw new InternalServerException("이미지 업로드 중 에러가 발생했습니다. 다시 시도해 주세요");
        }

        return successImages;
    }

    private void validateImage(List<MultipartFile> images) {
        if (MAX_IMAGE_LENGTH < images.size()) {
            throw new BadRequestException(TOO_MANY_FILE);
        }
        images.forEach(FileUtil::validateFileExt);
    }

    private ImageSaveResponse uploadInternal(MultipartFile image) {
        String originName = image.getOriginalFilename();
        String serverName = FileUtil.createServerName();
        String ext = FileUtil.getExt(originName);

        // 이미지 업로드
        try {
            storage.createFrom(BlobInfo.newBuilder(bucketName, serverName)
                            .setContentType(ext).build()
                    , image.getInputStream());
        } catch (IOException | StorageException ex) {
            log.error("GCP에 업로드 실패", ex);
            throw new InternalServerException("GCP에 이미지 업로드 실패..");
        }

        return new ImageSaveResponse(originName, serverName);
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
