package com.airplane.userpost.mapper;

import com.airplane.userpost.dto.UserDTO;
import com.airplane.userpost.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserDTO toDTO(User user) {
        return new UserDTO(
                user.getId(),
                user.getUserName(),
                user.getEmail(),
                user.getCreatedAt()
        );
    }

    //No copying id and createdAt fields!
    public User toUser(UserDTO userDTO) {
        User user = new User();
        user.setUserName(userDTO.userName());
        user.setEmail(userDTO.email());
        return user;
    }
}
