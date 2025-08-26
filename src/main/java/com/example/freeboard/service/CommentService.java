package com.example.freeboard.service;

import com.example.freeboard.dto.AdminCommentResponseDto;
import com.example.freeboard.dto.CommentCreateRequest;
import com.example.freeboard.dto.CommentResponseDto;
import com.example.freeboard.dto.CommentUpdateRequest;
import com.example.freeboard.entity.Comment;
import com.example.freeboard.entity.CommentLike;
import com.example.freeboard.entity.Post;
import com.example.freeboard.entity.User;
import com.example.freeboard.exception.ResourceNotFoundException;
import com.example.freeboard.repository.CommentLikeRepository;
import com.example.freeboard.repository.CommentRepository;
import com.example.freeboard.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final CommentLikeRepository commentLikeRepository;

    @Autowired
    public CommentService(CommentRepository commentRepository, PostRepository postRepository, CommentLikeRepository commentLikeRepository) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.commentLikeRepository = commentLikeRepository;
    }

    @Transactional(readOnly = true)
    public List<CommentResponseDto> getCommentsByPostId(Long postId, User currentUser) {
        List<Comment> comments = commentRepository.findByPostId(postId);
        return comments.stream()
                .map(comment -> {
                    // KEY POINT: 각 댓글마다 현재 로그인한 사용자의 '좋아요' 여부를 확인합니다.
                    boolean likedByCurrentUser = (currentUser != null) && commentLikeRepository.existsByCommentAndUser(comment, currentUser);
                    long likeCount = comment.getLikeCount() != null ? comment.getLikeCount() : 0L;
                    // KEY POINT: DTO 생성 시 '좋아요' 상태를 함께 전달합니다.
                    return new CommentResponseDto(comment, likedByCurrentUser, likeCount);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public Comment createComment(Long postId, CommentCreateRequest commentRequest, User author) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("게시글을 찾을 수 없습니다. (ID: " + postId + ")"));

        Comment comment = new Comment();
        comment.setPost(post);
        comment.setAuthor(author);
        comment.setContent(commentRequest.getContent());
        comment.setLikeCount(0);
        return commentRepository.save(comment);
    }

    @Transactional
    public CommentResponseDto toggleCommentLike(Long commentId, User currentUser) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("댓글을 찾을 수 없습니다. (ID: " + commentId + ")"));

        Optional<CommentLike> existingLike = commentLikeRepository.findByCommentAndUser(comment, currentUser);

        if (existingLike.isPresent()) {
            commentLikeRepository.delete(existingLike.get());
            comment.setLikeCount(Math.max(0, comment.getLikeCount() - 1));
        } else {
            CommentLike newLike = new CommentLike();
            newLike.setComment(comment);
            newLike.setUser(currentUser);
            commentLikeRepository.save(newLike);
            comment.setLikeCount(comment.getLikeCount() + 1);
        }
        Comment updatedComment = commentRepository.save(comment);

        boolean likedByCurrentUser = commentLikeRepository.existsByCommentAndUser(updatedComment, currentUser);
        return new CommentResponseDto(updatedComment, likedByCurrentUser, (long) updatedComment.getLikeCount());
    }

    @Transactional
    public CommentResponseDto updateComment(Long id, CommentUpdateRequest commentRequest, User currentUser) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("댓글을 찾을 수 없습니다. (ID: " + id + ")"));

        if (!comment.getAuthor().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("이 댓글을 수정할 권한이 없습니다.");
        }
        comment.setContent(commentRequest.getContent());
        Comment updatedComment = commentRepository.save(comment);

        boolean likedByCurrentUser = commentLikeRepository.existsByCommentAndUser(updatedComment, currentUser);
        return new CommentResponseDto(updatedComment, likedByCurrentUser, (long) updatedComment.getLikeCount());
    }

    @Transactional
    public void deleteComment(Long id, User currentUser) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("댓글을 찾을 수 없습니다. (ID: " + id + ")"));

        if (!comment.getAuthor().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("이 댓글을 삭제할 권한이 없습니다.");
        }
        commentRepository.delete(comment);
    }

    @Transactional
    public void deleteCommentAsAdmin(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("댓글을 찾을 수 없습니다. (ID: " + commentId + ")"));

        commentRepository.delete(comment);
    }

    @Transactional(readOnly = true)
    public Page<AdminCommentResponseDto> getAllCommentsForAdmin(Pageable pageable, String search) {
        Page<Comment> commentsPage;
        if (search != null && !search.trim().isEmpty()) {
            commentsPage = commentRepository.findByContentContainingIgnoreCaseForAdmin(search, pageable);
        } else {
            commentsPage = commentRepository.findAllForAdmin(pageable);
        }
        return commentsPage.map(AdminCommentResponseDto::fromEntity);
    }
}