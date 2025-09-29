// src/main/java/com/example/freeboard/repository/PostRepository.java
package com.example.freeboard.repository;

import com.example.freeboard.entity.Post;
import com.example.freeboard.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    // 모든 활성 게시글 조회
    @Query(value = "SELECT p FROM Post p JOIN FETCH p.author WHERE p.deleted = false",
            countQuery = "SELECT COUNT(p) FROM Post p WHERE p.deleted = false")
    Page<Post> findAllWithAuthor(Pageable pageable);

    // 단일 활성 게시글 조회
    @Query("SELECT p FROM Post p JOIN FETCH p.author WHERE p.id = :id AND p.deleted = false")
    Optional<Post> findByIdWithAuthor(@Param("id") Long id);

    // 검색 기능
    @Query(value = "SELECT p FROM Post p JOIN FETCH p.author WHERE p.deleted = false AND (LOWER(p.title) LIKE LOWER(CONCAT('%', :searchKeyword, '%')) OR LOWER(p.content) LIKE LOWER(CONCAT('%', :searchKeyword, '%')))",
            countQuery = "SELECT COUNT(p) FROM Post p WHERE p.deleted = false AND (LOWER(p.title) LIKE LOWER(CONCAT('%', :searchKeyword, '%')) OR LOWER(p.content) LIKE LOWER(CONCAT('%', :searchKeyword, '%')))")
    Page<Post> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCaseWithAuthor(@Param("searchKeyword") String searchKeyword, Pageable pageable);

    // --- 관리자 기능 ---
    @Query(value = "SELECT p FROM Post p JOIN FETCH p.author", countQuery = "SELECT COUNT(p) FROM Post p")
    Page<Post> findAllForAdmin(Pageable pageable);

    @Query(value = "SELECT p FROM Post p JOIN FETCH p.author WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :searchKeyword, '%')) OR LOWER(p.content) LIKE LOWER(CONCAT('%', :searchKeyword, '%'))",
            countQuery = "SELECT COUNT(p) FROM Post p WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :searchKeyword, '%')) OR LOWER(p.content) LIKE LOWER(CONCAT('%', :searchKeyword, '%'))")
    Page<Post> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCaseForAdmin(@Param("searchKeyword") String searchKeyword, Pageable pageable);

    @Modifying
    @Query("UPDATE Post p SET p.deleted = true WHERE p.id = :id")
    void softDeleteById(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Post p SET p.deleted = false WHERE p.id = :id")
    void restoreById(@Param("id") Long id);

    // === 사용자 삭제 메서드 ===
    @Transactional
    @Modifying
    void deleteByAuthor(User author);
}