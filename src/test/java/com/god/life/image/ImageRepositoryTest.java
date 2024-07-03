package com.god.life.image;

import com.god.life.config.JpaAuditingConfiguration;
import com.god.life.domain.Board;
import com.god.life.domain.Image;
import com.god.life.repository.BoardRepository;
import com.god.life.repository.ImageRepository;
import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

@ActiveProfiles("test")
@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.yaml")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaAuditingConfiguration.class)// 생성시간/수정시간 자동 주입 설정파일 임포트
public class ImageRepositoryTest {

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private EntityManager em;

    // @BeforeEach를 사용하면 테스트마다 rollback은 OK but, 테이블은 만들어진 상태라 PK값은 rollback 안됨.
    // afterEach 로 auto_increment 값 초기화 시켜주자.
    @BeforeEach
    void beforeEach(){
        for (int i = 0; i < 10; i++) {
            Board board = creatBoard(i);
            for (int j = 0; j < 3; j++) {
                Image image = Image.builder()
                        .serverName(j + "servername")
                        .originalName(j + "orginalname")
                        .board(board)
                        .build();
                imageRepository.save(image);
            }
        }
    }

    @AfterEach
    void afterEach(){
        //auto-increment 값 재조정
        em.createNativeQuery("alter table board auto_increment 1").executeUpdate();
        em.createNativeQuery("alter table image auto_increment 1").executeUpdate();
    }

    @Test
    void 미완성_게시물의_이미지_삭제_테스트(){
        //given
        List<Long> incompleteBoardNumber = List.of(1L, 3L, 5L);

        //when 미완성된 게시판 번호 삭제
        imageRepository.deleteByBoardIds(incompleteBoardNumber);

        List<Image> images = imageRepository.findAll();
        Assertions.assertThat(images.size())
                .isEqualTo(21);
    }

    @Test
    void 중간에_업로드_취소처리한_이미지_삭제_테스트(){
        //given
        String imageNameInContent1 = "1servername";
        String imageNameInContent2 = "2servername";
        String imageNameNotInContent = "0servername"; //게시물 업로드 중 취소한 이미지
        List<String> usedImageName = List.of(imageNameInContent1, imageNameInContent2);
        Long boardId = 1L;

        //when
        imageRepository.deleteUnusedImageOnBoard(usedImageName, boardId);

        //then : boardId에 저장된 이미지 개수는 총 2개여야 함.
        List<Image> images = imageRepository.findByBoardId(boardId);
        Assertions.assertThat(images.size()).isEqualTo(2);
        Assertions.assertThat(images.stream().map(Image::getServerName)).contains(imageNameInContent1, imageNameInContent2);
        Assertions.assertThat(images.stream().map(Image::getServerName)).doesNotContain(imageNameNotInContent);
    }

    @Test
    void 중간에_업로드_취소처리한_이미지가_없는_경우(){
        //given
        String imageNameInContent1 = "1servername";
        String imageNameInContent2 = "2servername";
        String imageNameInContent3 = "0servername";
        List<String> usedImageName = List.of(imageNameInContent1, imageNameInContent2, imageNameInContent3);
        Long boardId = 1L;

        //when
        imageRepository.deleteUnusedImageOnBoard(usedImageName, boardId);

        //then : boardId에 저장된 이미지 개수는 총 3개여야 함.
        List<Image> images = imageRepository.findByBoardId(boardId);
        Assertions.assertThat(images.size()).isEqualTo(3);
        Assertions.assertThat(images.stream().map(Image::getServerName))
                .contains(imageNameInContent1, imageNameInContent2, imageNameInContent3);
    }

    private Board creatBoard(int i) {
        Board board = Board.builder()
                .title(i + "title")
                .content(i + "content")
                .build();
        boardRepository.save(board);
        return board;
    }

}
