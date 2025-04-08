package com.airplane.userpost.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
public class UserDTO {
    private final Long id;

    @NotBlank(message = "Username is empty.")
    private final String userName;

    @NotBlank(message = "Email is empty.")
    @Email(message = "Invalid email format.")
    private final String email;

    @EqualsAndHashCode.Exclude
    private final LocalDateTime createdAt;

    @Valid
    private final Set<PostDTO> posts = new HashSet<>();

    public void addPost(PostDTO postDTO) {
        posts.add(postDTO);
    }

}
