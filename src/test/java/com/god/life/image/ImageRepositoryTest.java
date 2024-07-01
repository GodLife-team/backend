package com.god.life.image;

import com.god.life.RepositoryTest;
import com.god.life.config.JpaAuditingConfiguration;
import com.god.life.domain.Board;
import com.god.life.domain.BoardStatus;
import com.god.life.domain.Image;
import com.god.life.repository.BoardRepository;
import com.god.life.repository.ImageRepository;
import org.assertj.core.api.Assertions;
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

    @Test
    void 미완성_게시물의_이미지_삭제_테스트(){
        //given
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
        List<Long> incompleteBoardNumber = List.of(1L, 3L, 5L);

        //when 미완성된 게시판 번호 삭제
        imageRepository.deleteByBoardIds(incompleteBoardNumber);

        List<Image> images = imageRepository.findAll();
        Assertions.assertThat(images.size())
                .isEqualTo(21);
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
