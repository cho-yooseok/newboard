
// C:\Users\admin\Desktop\freeboard\freeboard\src\main\java\com\example\freeboard\service\UserService.java
package com.example.freeboard.service;

import com.example.freeboard.dto.RegisterRequest; // RegisterRequest 임포트
import com.example.freeboard.dto.UserDto;
import com.example.freeboard.entity.User;
import com.example.freeboard.entity.UserRole;
import com.example.freeboard.exception.ResourceNotFoundException;
import com.example.freeboard.repository.UserRepository;
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

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User registerUser(RegisterRequest registerRequest) { // RegisterRequest 사용
        if (userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 사용자 이름입니다.");
        }

        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setRole(UserRole.USER); // 기본 역할은 USER
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
        userRepository.delete(user);
    }

    /**
     * 관리자 권한으로 특정 사용자의 역할을 업데이트합니다.
     * @param userId 업데이트할 사용자의 ID
     * @param newRoleName 새로운 역할 이름 (예: "USER", "ADMIN")
     * @return 업데이트된 UserDto 객체
     * @throws ResourceNotFoundException 사용자를 찾을 수 없을 때 발생
     * @throws IllegalArgumentException 유효하지 않은 역할 이름일 때 발생
     */
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