package com.example.freeboard.service;

import com.example.freeboard.dto.PostCreateRequest;
import com.example.freeboard.dto.PostResponseDto;
import com.example.freeboard.dto.PostUpdateRequest;
import com.example.freeboard.entity.Post;
import com.example.freeboard.entity.PostLike;
import com.example.freeboard.entity.User;
import com.example.freeboard.repository.CommentRepository;
import com.example.freeboard.repository.PostLikeRepository;
import com.example.freeboard.repository.PostRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final CommentRepository commentRepository;

    public PostService(PostRepository postRepository, PostLikeRepository postLikeRepository, CommentRepository commentRepository) {
        this.postRepository = postRepository;
        this.postLikeRepository = postLikeRepository;
        this.commentRepository = commentRepository;
    }

    @Transactional(readOnly = true)
    public Page<PostResponseDto> getAllPosts(Pageable pageable, String searchKeyword) {
        Page<Post> postsPage;
        if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
            postsPage = postRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCaseWithAuthor(searchKeyword, pageable);
        } else {
            postsPage = postRepository.findAllWithAuthor(pageable);
        }
        return postsPage.map(post -> {
            Long likeCount = postLikeRepository.countByPost(post);
            Long commentCount = commentRepository.countByPostId(post.getId());
            return PostResponseDto.builder()
                    .id(post.getId())
                    .title(post.getTitle())
                    .authorUsername(post.getAuthor().getUsername())
                    .createdAt(post.getCreatedAt())
                    .viewCount(post.getViewCount())
                    .likeCount(likeCount)
                    .commentCount(commentCount)
                    .build();
        });
    }

    @Transactional
    public PostResponseDto getPostById(Long id, Optional<User> currentUserOpt) {
        Post post = postRepository.findByIdWithAuthor(id)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다: " + id));
        post.setViewCount(post.getViewCount() + 1);
        postRepository.save(post);

        // KEY POINT: 현재 로그인한 사용자가 '좋아요'를 눌렀는지 확인합니다.
        boolean isLiked = currentUserOpt
                .map(user -> postLikeRepository.existsByPostAndUser(post, user))
                .orElse(false);

        Long likeCount = postLikeRepository.countByPost(post);
        Long commentCount = commentRepository.countByPostId(post.getId());

        return PostResponseDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .authorUsername(post.getAuthor().getUsername())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .viewCount(post.getViewCount())
                .likeCount(likeCount)
                .commentCount(commentCount)
                .likedByCurrentUser(isLiked)
                .build();
    }


    @Transactional(readOnly = true)
    public PostResponseDto getPostByIdNoViewCount(Long id) {
        Post post = postRepository.findByIdWithAuthor(id)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다: " + id));
        Long likeCount = postLikeRepository.countByPost(post);
        Long commentCount = commentRepository.countByPostId(post.getId());
        return PostResponseDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .authorUsername(post.getAuthor().getUsername())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .viewCount(post.getViewCount())
                .likeCount(likeCount)
                .commentCount(commentCount)
                .build();
    }

    @Transactional
    public PostResponseDto createPost(PostCreateRequest postRequest, User author) {
        Post post = new Post();
        post.setTitle(postRequest.getTitle());
        post.setContent(postRequest.getContent());
        post.setAuthor(author);
        post = postRepository.save(post);
        return PostResponseDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .authorUsername(author.getUsername())
                .createdAt(post.getCreatedAt())
                .viewCount(post.getViewCount())
                .likeCount(0L)
                .commentCount(0L)
                .build();
    }

    @Transactional
    public PostResponseDto updatePost(Long id, PostUpdateRequest postRequest, User currentUser) {
        Post post = postRepository.findByIdWithAuthor(id)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다: " + id));

        if (!Objects.equals(post.getAuthor().getId(), currentUser.getId())) {
            throw new IllegalArgumentException("게시글 수정 권한이 없습니다.");
        }

        post.setTitle(postRequest.getTitle());
        post.setContent(postRequest.getContent());
        post = postRepository.save(post);
        Long likeCount = postLikeRepository.countByPost(post);
        Long commentCount = commentRepository.countByPostId(post.getId());
        return PostResponseDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .authorUsername(currentUser.getUsername())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .viewCount(post.getViewCount())
                .likeCount(likeCount)
                .commentCount(commentCount)
                .build();
    }

    @Transactional
    public void deletePost(Long id, User currentUser) {
        Post post = postRepository.findByIdWithAuthor(id)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다: " + id));

        if (!Objects.equals(post.getAuthor().getId(), currentUser.getId())) {
            throw new IllegalArgumentException("게시글 삭제 권한이 없습니다.");
        }
        postRepository.softDeleteById(id);
    }

    @Transactional
    public PostResponseDto togglePostLike(Long postId, User user) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다: " + postId));

        Optional<PostLike> existingLike = postLikeRepository.findByPostAndUser(post, user);

        if (existingLike.isPresent()) {
            postLikeRepository.delete(existingLike.get());
            post.setLikeCount(Math.max(0, post.getLikeCount() - 1));
        } else {
            PostLike newLike = new PostLike();
            newLike.setPost(post);
            newLike.setUser(user);
            postLikeRepository.save(newLike);
            post.setLikeCount(post.getLikeCount() + 1);
        }
        Post updatedPost = postRepository.save(post);

        Long commentCount = commentRepository.countByPostId(updatedPost.getId());
        boolean likedByCurrentUser = postLikeRepository.existsByPostAndUser(updatedPost, user);
        return PostResponseDto.builder()
                .id(updatedPost.getId())
                .title(updatedPost.getTitle())
                .content(updatedPost.getContent())
                .authorUsername(updatedPost.getAuthor().getUsername())
                .createdAt(updatedPost.getCreatedAt())
                .updatedAt(updatedPost.getUpdatedAt())
                .viewCount(updatedPost.getViewCount())
                .likeCount(updatedPost.getLikeCount().longValue())
                .likedByCurrentUser(likedByCurrentUser)
                .commentCount(commentCount)
                .build();
    }

    // --- 관리자 기능 ---
    @Transactional(readOnly = true)
    public Page<PostResponseDto> getAllPostsForAdmin(Pageable pageable, String searchKeyword) {
        Page<Post> postsPage;
        if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
            postsPage = postRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCaseForAdmin(searchKeyword, pageable);
        } else {
            postsPage = postRepository.findAllForAdmin(pageable);
        }
        return postsPage.map(post -> {
            Long likeCount = postLikeRepository.countByPost(post);
            Long commentCount = commentRepository.countByPostId(post.getId());
            return PostResponseDto.fromEntityForAdmin(post, likeCount, commentCount);
        });
    }

    @Transactional
    public void softDeletePostByAdmin(Long postId) {
        if (!postRepository.existsById(postId)) {
            throw new EntityNotFoundException("게시글을 찾을 수 없습니다: " + postId);
        }
        postRepository.softDeleteById(postId);
    }

    @Transactional
    public void restorePostByAdmin(Long postId) {
        if (!postRepository.existsById(postId)) {
            throw new EntityNotFoundException("게시글을 찾을 수 없습니다: " + postId);
        }
        postRepository.restoreById(postId);
    }

    @Transactional
    public void hardDeletePostByAdmin(Long postId) {
        if (!postRepository.existsById(postId)) {
            throw new EntityNotFoundException("게시글을 찾을 수 없습니다: " + postId);
        }
        postRepository.deleteById(postId);
    }
}