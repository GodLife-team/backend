package com.god.life.service;

import com.god.life.dto.ImageSaveResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ImageUploadService {

    ImageSaveResponse upload(MultipartFile file) throws IOException;

}
