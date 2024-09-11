package com.god.life.service;


import com.god.life.domain.Board;
import com.god.life.domain.Image;
import com.god.life.domain.Member;
import com.god.life.dto.image.ImageSaveResponse;
import com.god.life.error.ErrorMessage;
import com.god.life.error.NotFoundResource;
import com.god.life.repository.BoardRepository;
import com.god.life.repository.ImageRepository;
import com.god.life.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageService {

    private final ImageRepository imageRepository;
    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository; // dao만 단순히 접근하므로 repo를 사용

    @Transactional
    public void saveImage(ImageSaveResponse response, Member member, Board board) {
        Image image = response.toEntity(member, board);
        imageRepository.save(image);
    }

    @Transactional
    public void saveUserImage(ImageSaveResponse response, Member member) {
        Image image = response.toEntity(member);
        imageRepository.save(image);
    }

    @Transactional
    public void saveImage(ImageSaveResponse response, Member member, Long boardId) {
        Board board = boardRepository.findById(boardId).orElseThrow(
                () -> new NotFoundResource(ErrorMessage.INVALID_BOARD_MESSAGE.getErrorMessage()));

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

    @Transactional
    public void deleteUserImages(Member deleteMember) {
        imageRepository.deleteByMember(deleteMember);
    }

    // imageType에 해당하는 사진이 이미 있는지 확인하고 있다면 삭제,
    @Transactional
    public void deleteTypeImage(String imageType, Long memberId) {
        imageRepository.deleteImageType(imageType, memberId);
    }

    @Transactional
    public void deleteImages(List<Long> boardIds) {
        imageRepository.deleteByBoardIds(boardIds);
    }


    @Transactional
    public void deleteUnusedImageInHtml(String html, Long boardId, String thumbnail) {
        Document document = Jsoup.parse(html);
        Elements imgs = document.getElementsByTag("img");

        List<String> usedImageName = new ArrayList<>();
        if(thumbnail != null && !thumbnail.isBlank()){ //썸네일 이미지가 있다면
            usedImageName.add(thumbnail); // 썸내일 이미지 삭제 방지
        }

        for (Element img : imgs) {
            String imageServerPath = img.attr("src");
            int imageNameStartIdx = imageServerPath.lastIndexOf('/');
            if(imageNameStartIdx == -1) continue;

            String imageName = imageServerPath.substring(imageNameStartIdx + 1);
            usedImageName.add(imageName);
        }

        if(!usedImageName.isEmpty()){
            imageRepository.deleteUnusedImageOnBoard(usedImageName, boardId);
        }
    }

    @Transactional
    public void updateMemberProfileImage(ImageSaveResponse response, Member loginMember) {
        //회원의 profile 업데이트
        Member findMember = memberRepository.findById(loginMember.getId()).get();

        if (!StringUtils.hasText(findMember.getBackgroundName())) {
            Image image = Image.builder()
                    .originalName(response.getOriginalName())
                    .serverName(response.getServerName())
                    .member(loginMember)
                    .build();

            imageRepository.save(image);
        } else {
            Image image = imageRepository.findByMemberAndServerName(findMember, findMember.getProfileName())
                    .orElseThrow();
            image.updateImagesName(response);
        }

        findMember.updateProfileImageName(response.getServerName());
    }

    @Transactional
    public void updateMemberBackgroundImage(ImageSaveResponse response, Member loginMember) {
        //회원의 profile 업데이트
        Member findMember = memberRepository.findById(loginMember.getId()).get();

        if (!StringUtils.hasText(findMember.getBackgroundName())) {
            Image image = Image.builder()
                    .originalName(response.getOriginalName())
                    .serverName(response.getServerName())
                    .member(loginMember)
                    .build();

            imageRepository.save(image);
        } else {
            Image image = imageRepository.findByMemberAndServerName(findMember, findMember.getBackgroundName())
                    .orElseThrow();
            image.updateImagesName(response);
        }

        findMember.updateBackgroundImageName(response.getServerName());
    }

}
