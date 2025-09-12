package com.example.freeboard.dto;

import com.example.freeboard.entity.Post;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class PostResponseDto {
    private Long id;
    private String title;
    private String content;
    private String authorUsername;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer viewCount;
    private Long likeCount;
    private boolean likedByCurrentUser;
    private Long commentCount;
    private boolean deleted;

    // Post 엔티티를 DTO로 변환하는 정적 메서드 (관리자용 - 모든 필드 포함)
    public static PostResponseDto fromEntityForAdmin(Post post, Long likeCount, Long commentCount) {
        return PostResponseDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .authorUsername(post.getAuthor() != null ? post.getAuthor().getUsername() : "탈퇴한 사용자")
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .viewCount(post.getViewCount())
                .likeCount(likeCount)
                .commentCount(commentCount)
                .deleted(post.isDeleted()) // 삭제 상태 포함
                .likedByCurrentUser(false) // 관리자 목록에서는 이 값이 중요하지 않음
                .build();
    }
}