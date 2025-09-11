package com.example.freeboard.dto;

import com.example.freeboard.entity.Comment;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponseDto {
    private Long id;
    private String content;
    private String authorUsername;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long likeCount; // 좋아요 수 필드
    private boolean likedByCurrentUser; // 현재 로그인한 사용자가 좋아요를 눌렀는지 여부


    // 생성자 오버로딩 1: Comment 엔티티 + 좋아요 여부 + 좋아요 수를 직접 전달받아 DTO 생성
    public CommentResponseDto(Comment comment, boolean likedByCurrentUser, Long likeCount) {
        this.id = comment.getId();
        this.content = comment.getContent();
        this.authorUsername = comment.getAuthor().getUsername();
        this.createdAt = comment.getCreatedAt();
        this.updatedAt = comment.getUpdatedAt();
        this.likeCount = likeCount;
        this.likedByCurrentUser = likedByCurrentUser;
    }

    // 생성자 오버로딩 2: 좋아요 수를 엔티티에서 가져오고, 없으면 기본값 0으로 설정
    public CommentResponseDto(Comment comment, boolean likedByCurrentUser) {
        this.id = comment.getId();
        this.content = comment.getContent();
        this.authorUsername = comment.getAuthor().getUsername();
        this.createdAt = comment.getCreatedAt();
        this.updatedAt = comment.getUpdatedAt();
        this.likeCount = comment.getLikeCount() != null ? comment.getLikeCount() : 0L;
        this.likedByCurrentUser = likedByCurrentUser;
    }
}