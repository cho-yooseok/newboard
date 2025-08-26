package com.example.freeboard.mapper;

import com.example.freeboard.dto.UserDto;
import com.example.freeboard.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserDto toUserDto(User user) {
        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getRole().name(),
                user.getCreatedAt()
        );
    }
}