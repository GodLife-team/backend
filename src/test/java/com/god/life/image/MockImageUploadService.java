package com.god.life.image;

import com.god.life.dto.ImageSaveResponse;
import com.god.life.service.ImageUploadService;
import com.god.life.util.FileUtil;
import com.google.cloud.storage.BlobInfo;
import org.springframework.core.MethodParameter;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MockImageUploadService implements ImageUploadService  {

    private final Map<String, MultipartFile> storage = new HashMap<>();

    @Override
    public ImageSaveResponse upload(MultipartFile file) throws IOException {
        String serverName = FileUtil.createServerName();
        String originName = file.getOriginalFilename();
        String ext = FileUtil.getExt(originName);

        // 이미지 업로드
        storage.put(serverName, file);

        return new ImageSaveResponse(originName, serverName);
    }

    @Override
    public List<ImageSaveResponse> uploads(List<MultipartFile> Images) {
        List<ImageSaveResponse> responses = new ArrayList<>();

        for (MultipartFile image : Images) {
            try {
                responses.add(upload(image));
            } catch (IOException | IllegalArgumentException ex) {
                for (int i = 0; i < responses.size(); i++) { //사진 업로드에 실패했으면 이전까지 업로드했던 사진 예외
                    delete(responses.get(i).getServerName());
                }
                throw new IllegalArgumentException("사진 업로드에 실패했습니다. 다시 시도해 주세요.");
            }
        }

        return responses;
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
