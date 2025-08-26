// src/main/java/com/example/freeboard/repository/PostLikeRepository.java
package com.example.freeboard.repository;

import com.example.freeboard.entity.Post;
import com.example.freeboard.entity.PostLike;
import com.example.freeboard.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    Optional<PostLike> findByPostAndUser(Post post, User user);

    boolean existsByPostAndUser(Post post, User user);

    long countByPost(Post post);

    // === 사용자 삭제를 위해 추가된 메서드 ===
    @Transactional
    @Modifying
    void deleteByUser(User user);
}