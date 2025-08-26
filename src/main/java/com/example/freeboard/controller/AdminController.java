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

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final PostService postService;
    private final CommentService commentService;

    // --- 사용자 관리 ---

    @GetMapping("/users")
    public ResponseEntity<Page<UserDto>> getAllUsers(
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<UserDto> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/users/{userId}/role")
    public ResponseEntity<UserDto> updateUserRole(@PathVariable Long userId, @RequestParam String role) {
        UserDto updatedUser = userService.updateUserRole(userId, role);
        return ResponseEntity.ok(updatedUser);
    }


    // --- 게시글 관리 ---

    @GetMapping("/posts")
    public ResponseEntity<Page<PostResponseDto>> getAllPostsForAdmin(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) String search) {
        Page<PostResponseDto> posts = postService.getAllPostsForAdmin(pageable, search);
        return ResponseEntity.ok(posts);
    }

    @PostMapping("/posts/{postId}/soft-delete")
    public ResponseEntity<Void> softDeletePost(@PathVariable Long postId) {
        postService.softDeletePostByAdmin(postId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/posts/{postId}/restore")
    public ResponseEntity<Void> restorePost(@PathVariable Long postId) {
        postService.restorePostByAdmin(postId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/posts/{postId}/hard-delete")
    public ResponseEntity<Void> hardDeletePost(@PathVariable Long postId) {
        postService.hardDeletePostByAdmin(postId);
        return ResponseEntity.noContent().build();
    }

    // --- 댓글 관리 (추가된 부분) ---

    /**
     * 모든 댓글 목록을 (검색 기능 포함) 페이징하여 조회합니다.
     * URL: /api/admin/comments
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
     * 특정 댓글을 관리자 권한으로 삭제합니다. (영구 삭제)
     * URL: /api/admin/comments/{commentId}
     * 권한: ROLE_ADMIN
     */
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteCommentAsAdmin(@PathVariable Long commentId) {
        commentService.deleteCommentAsAdmin(commentId);
        return ResponseEntity.noContent().build();
    }
}