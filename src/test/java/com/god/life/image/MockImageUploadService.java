package com.god.life.image;

import com.god.life.dto.image.ImageSaveResponse;
import com.god.life.error.BadRequestException;
import com.god.life.error.InternalServerException;
import com.god.life.service.ImageUploadService;
import com.god.life.util.FileUtil;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class MockImageUploadService implements ImageUploadService  {

    private final Map<String, MultipartFile> storage = new ConcurrentHashMap<>();
    private final ThreadPoolTaskExecutor executor;

    private final int expectValue;
    private AtomicInteger count = new AtomicInteger(1);


    public MockImageUploadService(int exception_count) {
        this.executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.initialize();
        this.expectValue = exception_count;
    }

    @Override
    public ImageSaveResponse upload(MultipartFile file) {
        int currentCount = count.getAndIncrement();
        if (currentCount == expectValue) {
            throw new InternalServerException("세 번째 업로드에서 예외 발생");
        }

        FileUtil.validateFileExt(file);
        String originName = file.getOriginalFilename();
        String serverName = FileUtil.createServerName(file.getOriginalFilename());

        // 이미지 업로드
        storage.put(serverName, file);

        return new ImageSaveResponse(originName, serverName);
    }

    @Override
    public List<ImageSaveResponse> uploads(List<MultipartFile> images) {
       validateImage(images);

        List<CompletableFuture<ImageSaveResponse>> futures = images.stream()
                .map(image -> CompletableFuture.supplyAsync(() -> upload(image), executor)
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
            try {
                Thread.sleep(1000); //삭제 대기 --> 테스트 용도
            } catch (InterruptedException ex) {
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


    @Override
    public void delete(String fileName) {
        storage.remove(fileName);
    }

    @Override
    public List<String> getAllImageNames() {
        return storage.keySet().stream().toList();
    }
}
