package com.god.life.image;

import com.god.life.error.BadRequestException;
import com.god.life.error.InternalServerException;
import com.god.life.service.ImageUploadService;
import org.apache.catalina.connector.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;


public class ImageUploadTest {

    private ImageUploadService mockImageUploadService;
    private List<MultipartFile> mockFiles;

    @BeforeEach
    public void setup() {
        //mockImageUploadService = spy(new MockImageUploadService());
        mockImageUploadService = new MockImageUploadService(100);
        mockFiles = new ArrayList<>();
    }

    @Test
    @DisplayName("이미지 한장이 정상적으로 업로드 되는지 확인한다.")
    public void upload_method_test() {
        //파일 한장 업로드
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getOriginalFilename()).thenReturn("file1.jpg");
        when(mockFile.getContentType()).thenReturn("image/");

        try {
            mockImageUploadService.upload(mockFile);
        } catch (RuntimeException exception) {
            Assertions.fail("jpg 사진은 정상 등록되어야 함.");
        }

        List<String> allImageNames = mockImageUploadService.getAllImageNames();
        Assertions.assertEquals(allImageNames.size(), 1); //한장만 업로드 하므로
    }

    @Test
    @DisplayName("파일 한장의 확장자가 사진 형식이 아닌경우 에러를 반환해야 한다.")
    public void upload_file_fail_test() {
        //동영상 하나 업로드
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getOriginalFilename()).thenReturn("file1.mp4");
        when(mockFile.getContentType()).thenReturn("audio/mpeg");


        Exception exception = Assertions.assertThrows(BadRequestException.class, () -> {
            mockImageUploadService.upload(mockFile);
        });

        Assertions.assertEquals(exception.getClass(), BadRequestException.class);
        Assertions.assertEquals(mockImageUploadService.getAllImageNames().size(), 0);
    }


    @Test
    @DisplayName("여러 장의 파일 중 3번째 파일이 사진 형식이 아닌 경우 예외를 반환하고, 저장된 사진은 0장이어야 한다.")
    public void multiple_file_upload_fail_with_wrong_ext(){
        String[] ext = {".jpg", ".jpeg", ".mp4", ".png"};
        for (int i = 0; i < 4; i++) {
            MultipartFile mockFile = mock(MultipartFile.class);
            if(i == 3) {
                lenient().when(mockFile.getContentType()).thenReturn("audio/mpeg"); //이미지
            }else{
                lenient().when(mockFile.getContentType()).thenReturn("image/");
            }
            lenient().when(mockFile.getOriginalFilename()).thenReturn("file" + i + ext[i]);
            mockFiles.add(mockFile);
        }

        Assertions.assertThrows(BadRequestException.class,() -> mockImageUploadService.uploads(mockFiles));
        List<String> fileNames = mockImageUploadService.getAllImageNames();
        Assertions.assertEquals(fileNames.size(), 0); // 업로드 중 실패하면 기존에 업로드한 사진을 지우므로 0장이 되어야 함
    }

    @Test
    @DisplayName("여러 장 사진 성공 테스트 케이스")
    public void multiple_file_upload_success(){
        String[] ext = {".jpg", ".jpeg", ".png", ".png"};
        //given
        for (int i = 0; i < 4; i++) {
            MultipartFile mockFile = mock(MultipartFile.class);
            lenient().when(mockFile.getContentType()).thenReturn("image/");
            lenient().when(mockFile.getOriginalFilename()).thenReturn("file" + i + ext[i]);
            mockFiles.add(mockFile);
        }

        //when
        mockImageUploadService.uploads(mockFiles);

        //then
        List<String> uploadedImages = mockImageUploadService.getAllImageNames();
        org.assertj.core.api.Assertions.assertThat(uploadedImages).size().isEqualTo(4);
    }


    @Test
    @DisplayName("여러 장의 파일 업로드 중 3번째 파일에서 예외를 반환하고, 저장된 사진은 0장이어야 한다.")
    public void multiple_file_upload_fail() {
        ImageUploadService mockImageUploadService = new MockImageUploadService(3);
        mockFiles = new ArrayList<>();
        String[] ext = {".jpg", ".jpeg", ".jpg", ".png"};
        for (int i = 0; i < 4; i++) {
            MultipartFile mockFile = mock(MultipartFile.class);
            lenient().when(mockFile.getOriginalFilename()).thenReturn("file" + i + ext[i]);
            lenient().when(mockFile.getContentType()).thenReturn("image/");
            mockFiles.add(mockFile);
        }


        // When
        Assertions.assertThrows(InternalServerException.class,
                () -> mockImageUploadService.uploads(mockFiles));

        List<String> savedSize = mockImageUploadService.getAllImageNames();
        org.assertj.core.api.Assertions.assertThat(savedSize).isEmpty();
    }

}
