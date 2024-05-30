package com.god.life.service;

import com.god.life.domain.Board;
import com.god.life.domain.Member;
import com.god.life.dto.*;
import com.god.life.error.ForbiddenException;
import com.god.life.error.NotFoundResource;
import com.god.life.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final ImageService imageService;
    private static final int PAGE_SIZE = 10;

    @Transactional
    public Long createBoard(BoardCreateRequest request, Member loginMember, List<ImageSaveResponse> uploadResponse) {
        // DB entity 생성
        Board board = request.toBoard(loginMember);
        boardRepository.save(board);

        for (ImageSaveResponse response : uploadResponse) {
            imageService.saveImage(response, loginMember, board);
        }

        //생성된 게시판 ID 반환
        return board.getId();
    }


    public BoardResponse detailBoard(Long boardId, Member loginMember) {
        Board board = boardRepository.findByIdWithMember(boardId)
                .orElseThrow(() -> new NotFoundResource("존재하지 않는 게시판입니다."));

        boolean isOwner = board.getMember().getId().equals(loginMember.getId()); // 작성자와 현재 로그인한 사람이 동일인인지

        return BoardResponse.of(board, isOwner);
    }

    public void checkAuthorization(Member member, Long boardId) {
        Board board = boardRepository.findByIdWithMember(boardId)
                .orElseThrow(() -> new NotFoundResource("존재하지 않는 게시판입니다."));

        if (!member.getId().equals(board.getMember().getId())) {
            throw new ForbiddenException("수정 및 삭제 권한이 없습니다.");
        }

    }

    @Transactional
    public BoardResponse updateBoard(Long boardId, List<ImageSaveResponse> uploadResponse, BoardCreateRequest request, Member loginMember) {
        Board board = boardRepository.findById(boardId).get(); //권한 체크 로직에서 게시판이 있는지 확인하므로 바로 꺼내오기

        for (ImageSaveResponse response : uploadResponse) {
            imageService.saveImage(response, loginMember, board);
        }

        board.updateBoard(request);

        return BoardResponse.of(board, true);
    }

    @Transactional
    public boolean deleteBoard(Long boardId) {
        boardRepository.deleteById(boardId);
        return true;
    }

//    @Transactional(readOnly = true)
//    public List<BoardSearchResponse> getBoardList(BoardSearchRequest boardSearchRequest) {
//        Pageable pageable =
//                PageRequest
//                        .of((boardSearchRequest.getPage() - 1), 10, Sort.by("createDate").descending());
//
//        Page<Board> pagingBoard = boardRepository.findByBoardfetchjoin(pageable);
//
//        List<Board> boards = pagingBoard.getContent();
//
//        boards.stream()
//                .forEach(b -> {
//                    b.getComments();
//                    b.getImages();
//                    b.getMember().getImages();
//                });
//
//
//        return boards.stream().map(b -> BoardSearchResponse.of(b, false)).toList();
//    }

    @Transactional(readOnly = true)
    public List<BoardSearchResponse> getBoardList(BoardSearchRequest boardSearchRequest) {
        Pageable pageable =
                PageRequest
                        .of((boardSearchRequest.getPage() - 1), 10, Sort.by("createDate").descending());

        Page<Board> pagingBoard = boardRepository.findBoardWithSearchRequest(boardSearchRequest, pageable);
        List<Board> boards = pagingBoard.getContent();

        boards.stream()
                .forEach(b -> {
                    b.getComments();
                    b.getImages();
                    b.getMember().getImages();
                });

        if (boardSearchRequest.getNickname() != null && !boardSearchRequest.getNickname().isBlank()) {
            return boards.stream().
                    filter(b -> b.getMember().getNickname().equals(boardSearchRequest.getNickname()))
                    .map(b -> BoardSearchResponse.of(b, false)).toList();
        }

        return boards.stream().map(b -> BoardSearchResponse.of(b, false)).toList();
    }
}
