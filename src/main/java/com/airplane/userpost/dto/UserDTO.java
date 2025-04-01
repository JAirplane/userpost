package com.airplane.userpost.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
public class UserDTO {
    private final Long id;
    private final String userName;
    private final String email;
    private final LocalDateTime createdAt;
    private final Set<PostDTO> posts = new HashSet<>();

    public void addPost(PostDTO postDTO) {
        posts.add(postDTO);
    }

}
