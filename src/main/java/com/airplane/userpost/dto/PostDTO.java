package com.airplane.userpost.dto;

import lombok.Data;

import java.time.LocalDateTime;

public record PostDTO(Long id, String title, String text,
        LocalDateTime createdAt, Long userId) {
}
