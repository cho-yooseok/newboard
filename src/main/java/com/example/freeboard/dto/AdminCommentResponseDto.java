package com.example.freeboard.dto;

import com.example.freeboard.entity.Comment;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdminCommentResponseDto {
    private Long id;
    private String content;
    private String authorUsername;
    private LocalDateTime createdAt;
    private Long postId;
    private String postTitle;

    public static AdminCommentResponseDto fromEntity(Comment comment) {
        return AdminCommentResponseDto.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .authorUsername(comment.getAuthor() != null ? comment.getAuthor().getUsername() : "탈퇴한 사용자")
                .createdAt(comment.getCreatedAt())
                .postId(comment.getPost() != null ? comment.getPost().getId() : null)
                .postTitle(comment.getPost() != null ? comment.getPost().getTitle() : "삭제된 게시글")
                .build();
    }
}