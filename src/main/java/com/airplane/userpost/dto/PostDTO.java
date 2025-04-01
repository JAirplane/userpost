package com.airplane.userpost.dto;

import lombok.Data;

import java.time.LocalDateTime;

public record PostDTO(Long id, String userName, String email,
        LocalDateTime createdAt, Long userId) {
}
