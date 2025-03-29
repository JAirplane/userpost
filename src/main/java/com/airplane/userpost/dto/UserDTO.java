package com.airplane.userpost.dto;

import java.time.LocalDateTime;

public record UserDTO(Long id,
                      String userName,
                      String email,
                      LocalDateTime createdAt
) {}
