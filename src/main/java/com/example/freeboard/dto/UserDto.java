// C:\Users\admin\Desktop\freeboard\freeboard\src\main\java\com\example\freeboard\dto\UserDto.java
package com.example.freeboard.dto;

import com.example.freeboard.entity.User;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;
    private String username;
    private String role; // <-- UserRole에서 String으로 타입 변경
    private LocalDateTime createdAt;

    public static UserDto fromEntity(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        // User 엔티티의 UserRole enum 값을 문자열로 변환하여 UserDto의 String role 필드에 설정
        dto.setRole(user.getRole().name());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }
}