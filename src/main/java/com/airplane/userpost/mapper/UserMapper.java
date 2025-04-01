package com.airplane.userpost.mapper;

import com.airplane.userpost.dto.PostDTO;
import com.airplane.userpost.dto.UserDTO;
import com.airplane.userpost.model.Post;
import com.airplane.userpost.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserDTO toDTO(User user) {
        UserDTO userDTO = new UserDTO(
                user.getId(),
                user.getUserName(),
                user.getEmail(),
                user.getCreatedAt()
        );

        for(Post post: user.getPosts()) {
            userDTO.addPost(new PostDTO(
                    post.getId(),
                    post.getTitle(),
                    post.getText(),
                    post.getCreatedAt(),
                    user.getId()
            ));
        }

        return userDTO;
    }

    //No copying id and createdAt fields!
    public User toUser(UserDTO userDTO) {
        User user = new User();
        user.setUserName(userDTO.getUserName());
        user.setEmail(userDTO.getEmail());
        for(PostDTO post: userDTO.getPosts()) {
            user.addPost(new Post());
        }
        return user;
    }
}
