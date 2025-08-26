package com.example.freeboard.controller;

import com.example.freeboard.dto.RegisterRequest; // 회원가입 요청 DTO (기존 사용 DTO)
import com.example.freeboard.dto.AuthRequest; // 로그인 요청 DTO (기존 사용 DTO)
import com.example.freeboard.dto.JwtAuthenticationResponse; // JWT 응답 DTO
import com.example.freeboard.dto.UserDto; // 사용자 정보 DTO
import com.example.freeboard.entity.User; // User 엔티티 임포트
import com.example.freeboard.security.JwtTokenProvider;
import com.example.freeboard.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority; // GrantedAuthority 임포트
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    public AuthController(UserService userService, AuthenticationManager authenticationManager, JwtTokenProvider tokenProvider) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    /**
     * 새로운 사용자를 등록하는 엔드포인트입니다.
     * @param registerRequest 사용자 등록 요청 DTO
     * @return 성공 또는 실패 메시지를 포함한 응답
     */
    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            // UserService의 registerUser 메서드 호출 (RegisterRequest를 인자로 받도록 서비스도 조정 필요)
            // 참고: UserService의 registerUser는 현재 UserRegisterDto를 받도록 되어 있을 수 있으므로,
            // 이 부분을 RegisterRequest로 맞추거나, AuthController에서 UserRegisterDto로 변환해야 합니다.
            // 여기서는 registerRequest가 UserService의 registerUser에 바로 전달된다고 가정합니다.
            userService.registerUser(registerRequest); // Assuming UserService has registerUser(RegisterRequest)
            return ResponseEntity.status(HttpStatus.CREATED).body("회원가입이 성공적으로 완료되었습니다!");
        } catch (IllegalArgumentException e) {
            // 사용자 이름 중복 등 비즈니스 로직 예외 처리
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            // 그 외 예상치 못한 서버 오류
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("회원가입 중 예상치 못한 오류가 발생했습니다.");
        }
    }

    /**
     * 사용자 로그인을 처리하고 JWT 토큰을 발급하는 엔드포인트입니다.
     * @param authRequest 로그인 요청 DTO (사용자 이름, 비밀번호 포함)
     * @return JWT 토큰과 사용자 정보를 포함한 응답
     */
    @PostMapping("/login")
    public ResponseEntity<JwtAuthenticationResponse> authenticateUser(@Valid @RequestBody AuthRequest authRequest) {
        // 1. Spring Security의 AuthenticationManager를 통해 사용자 인증을 시도합니다.
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authRequest.getUsername(),
                        authRequest.getPassword()
                )
        );

        // 2. 인증된 사용자 주체(Principal)에서 UserDetails를 가져옵니다.
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String authenticatedUsername = userDetails.getUsername();

        // 3. 사용자의 역할(Role) 정보를 가져옵니다.
        // Spring Security의 GrantedAuthority에서 첫 번째 역할(ROLE_ADMIN, ROLE_USER 등)을 추출합니다.
        String userRole = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority) // GrantedAuthority 객체에서 권한 문자열을 추출
                .findFirst() // 여러 권한 중 첫 번째를 가져옵니다. (보통 단일 역할일 경우 사용)
                .orElse("ROLE_USER"); // 역할이 없는 경우 기본값으로 "ROLE_USER" 설정

        // 4. "ROLE_" 접두사를 제거하여 프론트엔드에서 'ADMIN', 'USER' 등으로 사용하기 용이하게 만듭니다.
        if (userRole.startsWith("ROLE_")) {
            userRole = userRole.substring(5);
        }

        // 5. JWT 토큰을 생성합니다.
        String jwt = tokenProvider.generateToken(authentication);

        // 6. 생성된 JWT 토큰, 토큰 타입, 사용자 이름, 역할 정보를 포함하여 응답합니다.
        return ResponseEntity.ok(new JwtAuthenticationResponse(jwt, "Bearer", authenticatedUsername, userRole));
    }

    /**
     * 현재 로그인된 사용자의 상세 정보를 반환하는 엔드포인트입니다.
     * 이 엔드포인트는 토큰 유효성 검사 및 사용자 역할 확인에 사용됩니다.
     * @param authentication 현재 인증된 사용자의 Authentication 객체
     * @return UserDto (사용자 ID, 사용자 이름, 역할 포함)
     */
    @GetMapping("/me")
    public ResponseEntity<UserDto> getLoggedInUserInfo(Authentication authentication) {
        String username = authentication.getName(); // 현재 인증된 사용자의 사용자 이름
        // UserService를 통해 사용자 엔티티를 조회합니다.
        Optional<User> userOptional = userService.findByUsername(username);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            // User 엔티티를 UserDto로 변환합니다.
            // UserDto의 fromEntity 메서드는 UserRole을 String으로 변환하여 role 필드에 설정합니다.
            UserDto userDto = UserDto.fromEntity(user);
            return ResponseEntity.ok(userDto);
        }
        // 사용자를 찾을 수 없는 경우 404 Not Found 응답을 반환합니다.
        return ResponseEntity.notFound().build();
    }
}