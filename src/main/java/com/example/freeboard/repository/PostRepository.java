// src/main/java/com/example/freeboard/repository/PostRepository.java
package com.example.freeboard.repository;

import com.example.freeboard.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    // (기존) 모든 활성 게시글 조회 시 author 정보를 Eagerly Fetch (Fetch Join)
    @Query(value = "SELECT p FROM Post p JOIN FETCH p.author WHERE p.deleted = false",
            countQuery = "SELECT COUNT(p) FROM Post p WHERE p.deleted = false")
    Page<Post> findAllWithAuthor(Pageable pageable);

    // (기존) 단일 활성 게시글 조회 시 author 정보를 Eagerly Fetch (Fetch Join)
    @Query("SELECT p FROM Post p JOIN FETCH p.author WHERE p.id = :id AND p.deleted = false")
    Optional<Post> findByIdWithAuthor(@Param("id") Long id);

    // (기존) 검색 기능 추가: 제목 또는 내용에 검색어가 포함된 활성 게시글 조회
    @Query(value = "SELECT p FROM Post p JOIN FETCH p.author WHERE p.deleted = false AND (LOWER(p.title) LIKE LOWER(CONCAT('%', :searchKeyword, '%')) OR LOWER(p.content) LIKE LOWER(CONCAT('%', :searchKeyword, '%')))",
            countQuery = "SELECT COUNT(p) FROM Post p WHERE p.deleted = false AND (LOWER(p.title) LIKE LOWER(CONCAT('%', :searchKeyword, '%')) OR LOWER(p.content) LIKE LOWER(CONCAT('%', :searchKeyword, '%')))")
    Page<Post> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCaseWithAuthor(@Param("searchKeyword") String searchKeyword, Pageable pageable);

    // --- 관리자 기능 추가 부분 ---

    // 관리자: 모든 게시글 (삭제된 게시글 포함) 조회
    @Query(value = "SELECT p FROM Post p JOIN FETCH p.author",
            countQuery = "SELECT COUNT(p) FROM Post p")
    Page<Post> findAllForAdmin(Pageable pageable);

    // 관리자: 제목 또는 내용으로 모든 게시글 (삭제된 게시글 포함) 검색
    @Query(value = "SELECT p FROM Post p JOIN FETCH p.author WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :searchKeyword, '%')) OR LOWER(p.content) LIKE LOWER(CONCAT('%', :searchKeyword, '%'))",
            countQuery = "SELECT COUNT(p) FROM Post p WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :searchKeyword, '%')) OR LOWER(p.content) LIKE LOWER(CONCAT('%', :searchKeyword, '%'))")
    Page<Post> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCaseForAdmin(@Param("searchKeyword") String searchKeyword, Pageable pageable);

    // 관리자: 게시글을 소프트 삭제 (deleted 필드를 true로 설정)
    @Modifying
    @Query("UPDATE Post p SET p.deleted = true WHERE p.id = :id")
    void softDeleteById(@Param("id") Long id);

    // 관리자: 소프트 삭제된 게시글을 복원 (deleted 필드를 false로 설정)
    @Modifying
    @Query("UPDATE Post p SET p.deleted = false WHERE p.id = :id")
    void restoreById(@Param("id") Long id);

    // 참고: JpaRepository의 deleteById(Long id) 메서드는 기본적으로 영구 삭제를 수행합니다.
    // 따라서 관리자의 영구 삭제를 위해 별도의 쿼리를 작성할 필요는 없지만, 명확성을 위해 Service 계층에서 이를 호출하도록 합니다.
}