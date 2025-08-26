package com.example.freeboard.repository;

import com.example.freeboard.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostId(Long postId);

    Long countByPostId(Long postId);

    // --- 관리자 기능 추가 ---
    @Query(value = "SELECT c FROM Comment c JOIN FETCH c.author JOIN FETCH c.post",
            countQuery = "SELECT COUNT(c) FROM Comment c")
    Page<Comment> findAllForAdmin(Pageable pageable);

    @Query(value = "SELECT c FROM Comment c JOIN FETCH c.author JOIN FETCH c.post WHERE LOWER(c.content) LIKE LOWER(CONCAT('%', :search, '%'))",
            countQuery = "SELECT COUNT(c) FROM Comment c WHERE LOWER(c.content) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Comment> findByContentContainingIgnoreCaseForAdmin(@Param("search") String search, Pageable pageable);
}