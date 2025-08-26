package com.example.freeboard.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate; // @CreatedDate 임포트
import org.springframework.data.jpa.domain.support.AuditingEntityListener; // AuditingEntityListener 임포트
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails; // UserDetails 임포트
import org.springframework.security.crypto.password.PasswordEncoder; // PasswordEncoder 임포트 (필요 시)

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

@Entity
@Table(name = "users") // 'user'는 예약어일 수 있으므로 'users'로 변경하는 것을 권장
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class) // 엔티티 리스너 활성화 (Auditing을 위해)
public class User implements UserDetails { // UserDetails 인터페이스 구현

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING) // Enum 타입을 String으로 DB에 저장
    @Column(nullable = false)
    // 이전 'Role role'에서 'UserRole role'로 변경
    private UserRole role; // 역할 필드 추가 (기본값은 서비스에서 설정 또는 @PrePersist에서 초기화)

    @CreatedDate // 엔티티 생성 시 자동으로 날짜가 들어감
    @Column(updatable = false) // 생성된 이후에는 업데이트되지 않음
    private LocalDateTime createdAt;

    // --- UserDetails 인터페이스 구현 시작 ---
    // Spring Security가 사용자 정보를 로드할 때 사용합니다.

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 사용자의 역할을 Spring Security의 GrantedAuthority로 변환합니다.
        // 예를 들어, USER -> ROLE_USER, ADMIN -> ROLE_ADMIN
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + this.role.name()));
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // 계정 만료 여부 (true: 만료되지 않음)
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // 계정 잠금 여부 (true: 잠금되지 않음)
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // 비밀번호 만료 여부 (true: 만료되지 않음)
    }

    @Override
    public boolean isEnabled() {
        return true; // 계정 활성화 여부 (true: 활성화됨)
    }

    // --- UserDetails 인터페이스 구현 끝 ---

    // 선택 사항: PasswordEncoder를 사용하여 비밀번호를 설정하는 헬퍼 메서드
    public void setEncodedPassword(String rawPassword, PasswordEncoder passwordEncoder) {
        this.password = passwordEncoder.encode(rawPassword);
    }
}