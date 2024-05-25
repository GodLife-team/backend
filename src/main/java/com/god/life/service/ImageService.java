package com.god.life.service;


import com.god.life.domain.Board;
import com.god.life.domain.Image;
import com.god.life.domain.Member;
import com.god.life.dto.ImageSaveResponse;
import com.god.life.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageService {

    private final ImageUploadService imageUploadService;
    private final ImageRepository imageRepository;
    private final ObjectProvider<ImageService> imageServiceProvider;

    public List<ImageSaveResponse> uploadImages(List<MultipartFile> images, Member member) {
        List<ImageSaveResponse> responses = new ArrayList<>();
        for (MultipartFile image : images) {
           responses.add(uploadImage(image, member));
        }

        return responses;
    }


    public ImageSaveResponse uploadImage(MultipartFile file, Member member) {
        ImageSaveResponse response = null;

        try {
            response = imageUploadService.upload(file);
        } catch (IOException ex) {
            throw new IllegalArgumentException("서버 내부 오류입니다. 다시 시도해 주세요.");
        }

//        // 여기까지 트랜잭션을 걸 필요가 있을까?
//        // ??????????????
//        ImageService imageService = imageServiceProvider.getObject();
//        imageService.saveImage(response, member);

        return response;
    }

    @Transactional
    public void saveImage(ImageSaveResponse response, Member member, Board board) {
        Image image = Image.builder().member(member)
                .originalName(response.getOriginalName())
                .serverName(response.getServerName())
                .board(board)
                .build();

        imageRepository.save(image);
    }

    public List<ImageSaveResponse> findImages(Long boardId) {
        List<Image> images = imageRepository.findByBoardId(boardId);
        return images.stream().map(ImageSaveResponse::from).toList();

    }

    public List<String> getAllImageNames() {
        return imageRepository.findAll().stream().map(Image::getServerName).toList();
    }

    // BoardId에 대해 등록된 이미지를 DB에서 모두 지운다
    // GCP에서의 삭제는 스케쥴러를 통해 수행됨
    @Transactional
    public void deleteImages(Long boardId) {
        imageRepository.deleteByBoardId(boardId);
    }
}
