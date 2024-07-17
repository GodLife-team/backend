package com.god.life.service;

import com.god.life.dto.image.ImageSaveResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public interface ImageUploadService {

    static final int MAX_IMAGE_LENGTH = 5;

    ImageSaveResponse upload(MultipartFile file);

    List<ImageSaveResponse> uploads(List<MultipartFile> images);

    void delete(String fileName);

    List<String> getAllImageNames();
}
