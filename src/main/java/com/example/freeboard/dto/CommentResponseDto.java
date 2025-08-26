package com.example.freeboard.dto;

import com.example.freeboard.entity.Comment;
import lombok.AllArgsConstructor; // AllArgsConstructor 추가
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
// 생성자 수정: likeCount 필드 추가
@AllArgsConstructor
public class CommentResponseDto {
    private Long id;
    private String content;
    private String authorUsername;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long likeCount; // 좋아요 수 필드 추가
    private boolean likedByCurrentUser; // 현재 로그인한 사용자가 좋아요를 눌렀는지 여부

    public CommentResponseDto(Comment comment, boolean likedByCurrentUser, Long likeCount) {
        this.id = comment.getId();
        this.content = comment.getContent();
        this.authorUsername = comment.getAuthor().getUsername();
        this.createdAt = comment.getCreatedAt();
        this.updatedAt = comment.getUpdatedAt();
        this.likeCount = likeCount; // 좋아요 수 설정
        this.likedByCurrentUser = likedByCurrentUser;
    }

    // Comment 엔티티와 좋아요 여부만으로 생성하는 생성자도 유지 (필요에 따라)
    public CommentResponseDto(Comment comment, boolean likedByCurrentUser) {
        this.id = comment.getId();
        this.content = comment.getContent();
        this.authorUsername = comment.getAuthor().getUsername();
        this.createdAt = comment.getCreatedAt();
        this.updatedAt = comment.getUpdatedAt();
        // 이 생성자에서는 likeCount를 0 또는 null로 초기화하거나, 별도로 설정해야 함
        this.likeCount = comment.getLikeCount() != null ? comment.getLikeCount() : 0L; // Comment 엔티티의 getLikeCount() 사용
        this.likedByCurrentUser = likedByCurrentUser;
    }
}