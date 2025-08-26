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
    Optional<CommentLike> findByCommentAndUser(Comment comment, User user);

    boolean existsByCommentAndUser(Comment comment, User user);

    Long countByComment(Comment comment);

    // === 사용자 삭제를 위해 추가된 메서드 ===
    @Transactional
    @Modifying
    void deleteByUser(User user);
}