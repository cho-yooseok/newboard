// src/main/java/com/example/freeboard/repository/CommentLikeRepository.java
package com.example.freeboard.repository;

import com.example.freeboard.entity.Comment;
import com.example.freeboard.entity.CommentLike;
import com.example.freeboard.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {
    // === 특정 댓글과 특정 사용자의 좋아요 찾기 ===
    Optional<CommentLike> findByCommentAndUser(Comment comment, User user);

    // === 특정 댓글과 특정 사용자의 좋아요 여부 확인 ===
    boolean existsByCommentAndUser(Comment comment, User user);

    // === 특정 사용자가 누른 모든 좋아요 삭제 ===
    @Transactional
    @Modifying
    void deleteByUser(User user);
}