package com.example.freeboard.controller;

import com.example.freeboard.dto.CommentCreateRequest;
import com.example.freeboard.dto.CommentResponseDto;
import com.example.freeboard.dto.CommentUpdateRequest;
import com.example.freeboard.entity.Comment;
import com.example.freeboard.entity.User;
import com.example.freeboard.service.CommentService;
import com.example.freeboard.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/posts/{postId}/comments")
public class CommentController {

    private final CommentService commentService;
    private final UserService userService;

    @Autowired
    public CommentController(CommentService commentService, UserService userService) {
        this.commentService = commentService;
        this.userService = userService;
    }

    // 특정 게시글의 댓글 조회 - 로그인 없이 접근 가능 (좋아요 상태를 위해 UserDetails 추가)
    @GetMapping
    public ResponseEntity<List<CommentResponseDto>> getCommentsByPostId(@PathVariable Long postId,
                                                                        @AuthenticationPrincipal(expression = "null") UserDetails userDetails) {
        User currentUser = null;
        if (userDetails != null) {
            currentUser = userService.findByUsername(userDetails.getUsername()).orElse(null);
        }
        // 서비스 메서드에 currentUser 전달하여 각 댓글의 좋아요 상태 포함
        List<CommentResponseDto> commentDtos = commentService.getCommentsByPostId(postId, currentUser);
        return ResponseEntity.ok(commentDtos);
    }

    // 댓글 생성 (로그인 후 접근 가능)
    @PostMapping
    public ResponseEntity<CommentResponseDto> createComment(
            @PathVariable Long postId,
            @Valid @RequestBody CommentCreateRequest commentRequest,
            @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("인증된 사용자를 찾을 수 없습니다."));

        Comment createdComment = commentService.createComment(postId, commentRequest, currentUser);
        // 댓글 생성 시에는 새로 생성된 댓글이므로 좋아요는 누르지 않은 상태 (false)로 DTO 반환
        return new ResponseEntity<>(new CommentResponseDto(createdComment, false), HttpStatus.CREATED);
    }

    // 댓글 좋아요 토글 엔드포인트
    @PostMapping("/{commentId}/like")
    public ResponseEntity<CommentResponseDto> toggleCommentLike(@PathVariable Long postId, // postId는 URL 경로를 위해 유지
                                                                @PathVariable Long commentId,
                                                                @AuthenticationPrincipal UserDetails userDetails) {
        // 로그인하지 않은 경우 401 Unauthorized 응답
        if (userDetails == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        // UserDetails에서 사용자 이름으로 User 엔티티 조회
        User currentUser = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("인증된 사용자를 찾을 수 없습니다."));

        // CommentService의 좋아요 토글 메서드 호출
        CommentResponseDto updatedComment = commentService.toggleCommentLike(commentId, currentUser);
        // 좋아요 상태가 변경된 후의 댓글 정보를 반환
        return ResponseEntity.ok(updatedComment);
    }

    // 댓글 수정 (작성자만 가능)
    @PutMapping("/{commentId}")
    public ResponseEntity<CommentResponseDto> updateComment(
            @PathVariable Long commentId,
            @Valid @RequestBody CommentUpdateRequest commentRequest,
            @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("인증된 사용자를 찾을 수 없습니다."));

        CommentResponseDto updatedCommentDto = commentService.updateComment(commentId, commentRequest, currentUser);
        return ResponseEntity.ok(updatedCommentDto);
    }

    // 댓글 삭제 (작성자만 가능)
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("인증된 사용자를 찾을 수 없습니다."));

        commentService.deleteComment(commentId, currentUser);
        return ResponseEntity.noContent().build();
    }
}