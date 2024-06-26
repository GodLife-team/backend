package com.god.life.service;

import com.god.life.domain.Board;
import com.god.life.domain.CategoryType;
import com.god.life.domain.Comment;
import com.god.life.domain.Member;
import com.god.life.dto.CommentCreateRequest;
import com.god.life.dto.CommentResponse;
import com.god.life.error.ErrorMessage;
import com.god.life.error.ForbiddenException;
import com.god.life.error.NotFoundResource;
import com.god.life.repository.BoardRepository;
import com.god.life.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@Transactional
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;

    public List<CommentResponse> getCommentsForBoard(Long boardId, Member member) {

        List<Comment> comments = commentRepository.findByBoardIdWithMember(boardId);
        List<CommentResponse> commentResponses =
                comments.stream().map(c -> CommentResponse.of(c, member.getId())).toList();

        return commentResponses;
    }

    @Transactional
    public CommentResponse createComment(Long boardId, CommentCreateRequest request, Member member) {
        Board board = boardRepository.findByIdAnyBoardType(boardId)
                .orElseThrow(() -> new NotFoundResource(ErrorMessage.INVALID_BOARD_MESSAGE.getErrorMessage()));

        Comment comment = request.toEntity(board, member);
        commentRepository.save(comment);

        return CommentResponse.of(comment, member.getId());
    }

    @Transactional
    public CommentResponse updateComment(Long commentId, CommentCreateRequest request, Member member){
        Comment comment = commentRepository.findByIdWithMember(commentId)
                .orElseThrow(() -> new NotFoundResource(ErrorMessage.INVALID_COMMENT_MESSAGE.getErrorMessage()));

        if (!comment.getMember().getId().equals(member.getId())) {
            throw new ForbiddenException(ErrorMessage.FORBIDDEN_ACTION_MESSAGE.getErrorMessage());
        }

        comment.updateComment(request.getComment());
        return CommentResponse.of(comment, member.getId());
    }

    @Transactional
    public void deleteComment(Long commentId, Member member) {
        Comment comment = commentRepository.findByIdWithMember(commentId)
                .orElseThrow(() -> new NotFoundResource(ErrorMessage.INVALID_COMMENT_MESSAGE.getErrorMessage()));

        if (!comment.getMember().getId().equals(member.getId())) {
            throw new ForbiddenException(ErrorMessage.FORBIDDEN_ACTION_MESSAGE.getErrorMessage());
        }

        commentRepository.delete(comment);
    }

    public void deleteCommentWrittenByMember(Member deleteMember) {
        commentRepository.deleteByMember(deleteMember);
    }
}
