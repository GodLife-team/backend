package com.god.life.service;

import com.god.life.dto.image.ImageSaveResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ImageUploadService {

    ImageSaveResponse upload(MultipartFile file) throws IOException;

    List<ImageSaveResponse> uploads(List<MultipartFile> file);

    void delete(String fileName);

    List<String> getAllImageNames();
}
