package com.example.freeboard.repository;

import com.example.freeboard.entity.CommentLike;
import com.example.freeboard.entity.Comment;
import com.example.freeboard.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying; // Modifying 어노테이션 추가
import org.springframework.data.jpa.repository.Query;    // Query 어노테이션 추가
import org.springframework.data.repository.query.Param; // Param 어노테이션 추가
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional; // Transactional 어노테이션 추가

import java.util.Optional;

@Repository
public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {
    Optional<CommentLike> findByCommentAndUser(Comment comment, User user);
    boolean existsByCommentAndUser(Comment comment, User user);
    Long countByComment(Comment comment); // 특정 댓글의 좋아요 수 계산 (반환 타입을 long에서 Long으로 명시)

    // 새로 추가할 메서드: Comment와 연관된 모든 CommentLike를 삭제
    @Modifying // 이 쿼리가 데이터를 수정(삭제)함을 나타냅니다.
    @Transactional // 이 작업이 트랜잭션 내에서 실행되도록 합니다.
    @Query("DELETE FROM CommentLike cl WHERE cl.comment = :comment")
    void deleteByComment(@Param("comment") Comment comment);
}