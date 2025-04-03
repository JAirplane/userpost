package com.airplane.userpost.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Objects;

public record PostDTO(Long id, String title, String text,
        LocalDateTime createdAt, Long userId) {

    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if(obj == null || this.getClass() != obj.getClass()) return false;

        PostDTO other = (PostDTO) obj;
        return Objects.equals(this.id, other.id)
                && Objects.equals(this.title, other.title)
                && Objects.equals(this.text, other.text)
                && Objects.equals(this.userId, other.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, text, userId);
    }
}
