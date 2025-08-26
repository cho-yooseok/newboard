// C:\Users\admin\Desktop\freeboard\freeboard\src\main\java\com\example\freeboard\service\UserService.java
package com.example.freeboard.service;

import com.example.freeboard.dto.RegisterRequest;
import com.example.freeboard.dto.UserDto;
import com.example.freeboard.entity.User;
import com.example.freeboard.entity.UserRole;
import com.example.freeboard.exception.ResourceNotFoundException;
import com.example.freeboard.repository.*; // repository 임포트 변경
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    // === 연관 데이터 삭제를 위해 Repository 주입 ===
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PostLikeRepository postLikeRepository;
    private final CommentLikeRepository commentLikeRepository;


    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, PostRepository postRepository, CommentRepository commentRepository, PostLikeRepository postLikeRepository, CommentLikeRepository commentLikeRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.postLikeRepository = postLikeRepository;
        this.commentLikeRepository = commentLikeRepository;
    }

    @Transactional
    public User registerUser(RegisterRequest registerRequest) {
        if (userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 사용자 이름입니다.");
        }

        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setRole(UserRole.USER);
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional
    public void changePassword(String username, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public Page<UserDto> getAllUsers(Pageable pageable) {
        Page<User> usersPage = userRepository.findAll(pageable);
        return usersPage.map(UserDto::fromEntity);
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다. (ID: " + userId + ")"));

        // 1. 사용자가 누른 '좋아요' 기록을 먼저 모두 삭제합니다.
        postLikeRepository.deleteByUser(user);
        commentLikeRepository.deleteByUser(user);

        // 2. 사용자가 작성한 댓글을 모두 삭제합니다.
        // (이 댓글에 달린 '좋아요'는 Comment 엔티티의 cascade 설정으로 자동 삭제됩니다.)
        commentRepository.deleteByAuthor(user);

        // 3. 사용자가 작성한 게시글을 모두 삭제합니다.
        // (이 게시글에 달린 댓글, '좋아요'는 Post 엔티티의 cascade 설정으로 자동 삭제됩니다.)
        postRepository.deleteByAuthor(user);

        // 4. 마지막으로 사용자를 삭제합니다.
        userRepository.delete(user);
    }

    @Transactional
    public UserDto updateUserRole(Long userId, String newRoleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다. (ID: " + userId + ")"));

        UserRole newRole;
        try {
            newRole = UserRole.valueOf(newRoleName.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("유효하지 않은 역할 이름입니다: " + newRoleName);
        }

        user.setRole(newRole);
        User updatedUser = userRepository.save(user);
        return UserDto.fromEntity(updatedUser);
    }
}