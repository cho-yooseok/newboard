// src/main/java/com/example/freeboard/repository/PostLikeRepository.java
package com.example.freeboard.repository;

import com.example.freeboard.entity.PostLike;
import com.example.freeboard.entity.Post;
import com.example.freeboard.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    Optional<PostLike> findByPostAndUser(Post post, User user);
    boolean existsByPostAndUser(Post post, User user);
    long countByPost(Post post); // 특정 게시글의 좋아요 수 계산
}