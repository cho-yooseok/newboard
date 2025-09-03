package com.example.freeboard.controller;

import com.example.freeboard.dto.AdminCommentResponseDto;
import com.example.freeboard.dto.PostResponseDto;
import com.example.freeboard.dto.UserDto;
import com.example.freeboard.service.CommentService;
import com.example.freeboard.service.PostService;
import com.example.freeboard.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController // REST API 컨트롤러임을 나타냄. (응답 값이 JSON 형식으로 반환됨)
@RequestMapping("/api/admin") // 관리자 전용 API의 공통 URL prefix
@PreAuthorize("hasRole('ADMIN')") // 클래스 레벨에서 ADMIN 권한을 가진 사용자만 접근 가능
@RequiredArgsConstructor // final 필드에 대한 생성자를 자동 생성 (DI)
public class AdminController {

    private final UserService userService;   // 사용자 관리 서비스
    private final PostService postService;   // 게시글 관리 서비스
    private final CommentService commentService; // 댓글 관리 서비스

    // --- 사용자 관리 ---

    /**
     * 전체 사용자 목록 조회 (페이징 처리)
     * URL: GET /api/admin/users
     * 권한: ROLE_ADMIN
     */
    @GetMapping("/users")
    public ResponseEntity<Page<UserDto>> getAllUsers(
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<UserDto> users = userService.getAllUsers(pageable); // 서비스에서 페이징 처리된 사용자 목록 가져오기
        return ResponseEntity.ok(users); // 200 OK와 함께 반환
    }

    /**
     * 특정 사용자 삭제
     * URL: DELETE /api/admin/users/{userId}
     * 권한: ROLE_ADMIN
     */
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId); // 사용자 삭제 로직 실행
        return ResponseEntity.noContent().build(); // 204 No Content 응답
    }

    /**
     * 특정 사용자의 권한(Role) 수정
     * URL: PUT /api/admin/users/{userId}/role?role=NEW_ROLE
     * 권한: ROLE_ADMIN
     */
    @PutMapping("/users/{userId}/role")
    public ResponseEntity<UserDto> updateUserRole(@PathVariable Long userId, @RequestParam String role) {
        UserDto updatedUser = userService.updateUserRole(userId, role); // 권한 업데이트
        return ResponseEntity.ok(updatedUser); // 업데이트된 사용자 정보 반환
    }


    // --- 게시글 관리 ---

    /**
     * 모든 게시글 조회 (검색 가능, 페이징 지원)
     * URL: GET /api/admin/posts?search=검색어
     * 권한: ROLE_ADMIN
     */
    @GetMapping("/posts")
    public ResponseEntity<Page<PostResponseDto>> getAllPostsForAdmin(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) String search) {
        Page<PostResponseDto> posts = postService.getAllPostsForAdmin(pageable, search);
        return ResponseEntity.ok(posts);
    }

    /**
     * 게시글 소프트 삭제 (DB에는 남아있지만 상태값을 변경하여 비활성화 처리)
     * URL: POST /api/admin/posts/{postId}/soft-delete
     */
    @PostMapping("/posts/{postId}/soft-delete")
    public ResponseEntity<Void> softDeletePost(@PathVariable Long postId) {
        postService.softDeletePostByAdmin(postId);
        return ResponseEntity.ok().build(); // 200 OK
    }

    /**
     * 소프트 삭제된 게시글 복구
     * URL: POST /api/admin/posts/{postId}/restore
     */
    @PostMapping("/posts/{postId}/restore")
    public ResponseEntity<Void> restorePost(@PathVariable Long postId) {
        postService.restorePostByAdmin(postId);
        return ResponseEntity.ok().build();
    }

    /**
     * 게시글 영구 삭제 (DB에서 완전히 삭제)
     * URL: DELETE /api/admin/posts/{postId}/hard-delete
     */
    @DeleteMapping("/posts/{postId}/hard-delete")
    public ResponseEntity<Void> hardDeletePost(@PathVariable Long postId) {
        postService.hardDeletePostByAdmin(postId);
        return ResponseEntity.noContent().build();
    }

    // --- 댓글 관리 ---

    /**
     * 모든 댓글 목록 조회 (검색 가능, 페이징 지원)
     * URL: GET /api/admin/comments?search=검색어
     * 권한: ROLE_ADMIN
     */
    @GetMapping("/comments")
    public ResponseEntity<Page<AdminCommentResponseDto>> getAllCommentsForAdmin(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) String search) {
        Page<AdminCommentResponseDto> comments = commentService.getAllCommentsForAdmin(pageable, search);
        return ResponseEntity.ok(comments);
    }

    /**
     * 특정 댓글 영구 삭제
     * URL: DELETE /api/admin/comments/{commentId}
     * 권한: ROLE_ADMIN
     */
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteCommentAsAdmin(@PathVariable Long commentId) {
        commentService.deleteCommentAsAdmin(commentId);
        return ResponseEntity.noContent().build();
    }
}
