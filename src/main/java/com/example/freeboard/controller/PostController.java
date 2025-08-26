package com.example.freeboard.controller;

import com.example.freeboard.dto.PostCreateRequest;
import com.example.freeboard.dto.PostResponseDto;
import com.example.freeboard.dto.PostUpdateRequest;
import com.example.freeboard.entity.User;
import com.example.freeboard.service.PostService;
import com.example.freeboard.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;
    private final UserService userService;

    public PostController(PostService postService, UserService userService) {
        this.postService = postService;
        this.userService = userService;
    }

    // 게시글 목록 조회 (검색 기능 포함)
    @GetMapping
    public ResponseEntity<Page<PostResponseDto>> getAllPosts(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) String search) {
        Page<PostResponseDto> posts = postService.getAllPosts(pageable, search);
        return ResponseEntity.ok(posts);
    }

    // 게시글 상세 조회 (조회수 증가 및 좋아요 상태 포함)
    @GetMapping("/{id}")
    public ResponseEntity<PostResponseDto> getPostById(@PathVariable Long id,
                                                       @AuthenticationPrincipal(expression = "null") UserDetails userDetails) {
        Optional<User> currentUserOpt = Optional.empty();
        if (userDetails != null) {
            currentUserOpt = userService.findByUsername(userDetails.getUsername());
        }

        // 서비스 계층으로 현재 사용자 정보를 넘겨서 DTO를 완성
        PostResponseDto postDto = postService.getPostById(id, currentUserOpt);
        return ResponseEntity.ok(postDto);
    }

    // 게시글 수정용 상세 조회 (조회수 증가 없음)
    @GetMapping("/{id}/edit")
    public ResponseEntity<PostResponseDto> getPostByIdForEdit(@PathVariable Long id) {
        PostResponseDto postDto = postService.getPostByIdNoViewCount(id);
        return ResponseEntity.ok(postDto);
    }

    // 게시글 좋아요 토글
    @PostMapping("/{postId}/like")
    public ResponseEntity<PostResponseDto> togglePostLike(@PathVariable Long postId,
                                                          @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        User currentUser = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("인증된 사용자를 찾을 수 없습니다."));

        PostResponseDto updatedPost = postService.togglePostLike(postId, currentUser);
        return ResponseEntity.ok(updatedPost);
    }

    // 게시글 생성
    @PostMapping
    public ResponseEntity<PostResponseDto> createPost(@Valid @RequestBody PostCreateRequest postRequest,
                                                      @AuthenticationPrincipal User currentUser) {
        PostResponseDto createdPost = postService.createPost(postRequest, currentUser);
        return new ResponseEntity<>(createdPost, HttpStatus.CREATED);
    }

    // 게시글 수정
    @PutMapping("/{id}")
    public ResponseEntity<PostResponseDto> updatePost(@PathVariable Long id,
                                                      @Valid @RequestBody PostUpdateRequest postRequest,
                                                      @AuthenticationPrincipal User currentUser) {
        PostResponseDto updatedPost = postService.updatePost(id, postRequest, currentUser);
        return ResponseEntity.ok(updatedPost);
    }

    // 게시글 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id,
                                           @AuthenticationPrincipal User currentUser) {
        postService.deletePost(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}