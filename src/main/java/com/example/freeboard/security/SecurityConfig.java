package com.example.freeboard.security;

import com.example.freeboard.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity; // @PreAuthorize 어노테이션 활성화를 위해 필요
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Spring Security 6.x 이상에서 @PreAuthorize 활성화를 위한 설정
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    public SecurityConfig(CustomUserDetailsService userDetailsService, JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // CSRF 비활성화 (JWT 사용 시 일반적으로 필요 없음)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 사용 안 함
                .authorizeHttpRequests(auth -> auth
                        // 1. HTML 파일 및 정적 리소스 접근 허용
                        .requestMatchers(
                                "/", // 루트 경로 (index.html 등)
                                "/*.html", // 모든 HTML 파일 (예: /login.html, /register.html 등)
                                "/css/**", // CSS 파일
                                "/js/**", // JavaScript 파일
                                "/img/**", // 이미지 파일
                                "/favicon.ico", // 파비콘
                                "/static/**" // static 디렉토리 내 모든 리소스
                        ).permitAll()

                        // 2. 공개 API 접근 허용 (인증 없이 접근 가능)
                        .requestMatchers("/api/auth/**").permitAll() // 회원가입, 로그인 관련 API
                        .requestMatchers(HttpMethod.GET, "/api/posts", "/api/posts/**").permitAll() // 게시글 목록 조회 및 특정 게시글 상세 조회
                        .requestMatchers(HttpMethod.GET, "/api/posts/*/comments").permitAll() // 특정 게시글의 댓글 목록 조회

                        // 3. 관리자 API는 ROLE_ADMIN만 접근 허용
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // 4. 로그인(인증)된 사용자만 접근 가능한 API
                        // 게시글 관련
                        .requestMatchers(HttpMethod.POST, "/api/posts").authenticated() // 글 작성
                        .requestMatchers(HttpMethod.POST, "/api/posts/*/like").authenticated() // 게시글 좋아요 토글
                        .requestMatchers(HttpMethod.PUT, "/api/posts/**").authenticated() // 글 수정
                        .requestMatchers(HttpMethod.DELETE, "/api/posts/**").authenticated() // 글 삭제 (일반 사용자용)

                        // 댓글 관련
                        .requestMatchers(HttpMethod.POST, "/api/posts/*/comments").authenticated() // 댓글 작성
                        .requestMatchers(HttpMethod.POST, "/api/posts/*/comments/*/like").authenticated() // 댓글 좋아요 토글
                        .requestMatchers(HttpMethod.PUT, "/api/posts/*/comments/**").authenticated() // 댓글 수정
                        .requestMatchers(HttpMethod.DELETE, "/api/posts/*/comments/**").authenticated() // 댓글 삭제

                        // 5. 그 외 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )
                // DaoAuthenticationProvider를 사용하여 사용자 인증 처리
                .authenticationProvider(authenticationProvider())
                // JWT 필터를 UsernamePasswordAuthenticationFilter 이전에 추가하여 요청 헤더의 JWT 토큰을 검증
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
