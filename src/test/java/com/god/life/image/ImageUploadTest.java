package com.god.life.image;

import com.god.life.service.ImageUploadService;
import org.apache.catalina.connector.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.multipart.MultipartFile;
import org.mockito.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;


public class ImageUploadTest {

    ImageUploadService mockImageUploadService;

    private List<MultipartFile> mockFiles;

    @BeforeEach
    public void setup() {
        mockImageUploadService = spy(new MockImageUploadService());
        mockFiles = new ArrayList<>();
    }

    @Test
    @DisplayName("이미지 한장이 정상적으로 업로드 되는지 확인한다.")
    public void upload_method_test() {
        //파일 한장 업로드
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getOriginalFilename()).thenReturn("file1.jpg");
        try {
            mockImageUploadService.upload(mockFile);
        } catch (IOException exception) {
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

        Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            mockImageUploadService.upload(mockFile);
        });

        Assertions.assertEquals(exception.getClass(), IllegalArgumentException.class);
        Assertions.assertEquals(mockImageUploadService.getAllImageNames().size(), 0);
    }


    @Test
    @DisplayName("여러 장의 파일 중 3번째 파일이 사진 형식이 아닌 경우 예외를 반환하고, 저장된 사진은 0장이어야 한다.")
    public void multiple_file_upload_fail_with_wrong_ext(){

        String[] ext = {".jpg", ".jpeg", ".mp4", ".png"};
        for (int i = 0; i < 4; i++) {
            MultipartFile mockFile = mock(MultipartFile.class);
            lenient().when(mockFile.getOriginalFilename()).thenReturn("file" + i + ext[i]); //바로 사용하지 않으면 지연처리
            mockFiles.add(mockFile);
        }

        Assertions.assertThrows(IllegalArgumentException.class,() -> mockImageUploadService.uploads(mockFiles));
        List<String> fileNames = mockImageUploadService.getAllImageNames();
        Assertions.assertEquals(fileNames.size(), 0); // 업로드 중 실패하면 기존에 업로드한 사진을 지우므로 0장이 되어야 함
    }


    @Test
    @DisplayName("여러 장의 파일 업로드 중 3번째 파일에서 예외를 반환하고, 저장된 사진은 0장이어야 한다.")
    public void multiple_file_upload_fail(){

        String[] ext = {".jpg", ".jpeg", ".jpg", ".png"};
        for (int i = 0; i < 4; i++) {
            MultipartFile mockFile = mock(MultipartFile.class);
            lenient().when(mockFile.getOriginalFilename()).thenReturn("file" + i + ext[i]);
            mockFiles.add(mockFile);
        }

        /**
         * doAnswer는 Mockito에서 사용되는 메서드
         * 모의 객체의 메서드가 호출될 때 실행할 커스텀 로직을 정의함
         * when : 모의 객체.XXX 모의 객체가 호출할 메소드 (파라미터)
         */
        doAnswer(invocation -> {
            List<MultipartFile> files = invocation.getArgument(0);
            for (MultipartFile file : files) {
                if (mockFiles.indexOf(file) == 2) {
                    throw new IOException("파일 업로드 도중 실패..!");
                }
            }
            return new ArrayList<Response>(); // 실제로는 적절한 반환값으로 대체
        }).when(mockImageUploadService).uploads(anyList());

        Assertions.assertThrows(IOException.class,
                () -> mockImageUploadService.uploads(mockFiles));
    }

}
