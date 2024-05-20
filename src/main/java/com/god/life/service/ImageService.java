package com.god.life.service;


import com.god.life.domain.Image;
import com.god.life.domain.Member;
import com.god.life.dto.ImageSaveResponse;
import com.god.life.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ServerErrorException;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageService {

    private final ImageUploadService imageService;
    private final ImageRepository imageRepository;

    public ImageSaveResponse save(MultipartFile file, Member member) {

        ImageSaveResponse response = null;

        try {
            response = imageService.upload(file);
        } catch (IOException ex) {
            throw new IllegalArgumentException("서버 내부 오류입니다. 다시 시도해 주세요.");
        }

        Image image = Image.builder().member(member)
                .originalName(response.getOriginalName())
                .serverName(response.getServerName())
                .build();

        image.updateUploader(member);
        imageRepository.save(image);

        return response;
    }

}
