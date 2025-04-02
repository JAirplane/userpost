package com.airplane.userpost.mapper;

import com.airplane.userpost.dto.PostDTO;
import com.airplane.userpost.dto.UserDTO;
import com.airplane.userpost.exception.MapperException;
import com.airplane.userpost.model.Post;
import com.airplane.userpost.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    private final PostMapper postMapper;

    @Autowired
    public UserMapper(PostMapper postMapper) {
        this.postMapper = postMapper;
    }

    public UserDTO toDTO(User user) {
        if(user == null) throw new MapperException("Mapper received null User.");

        UserDTO userDTO = new UserDTO(
                user.getId(),
                user.getUserName(),
                user.getEmail(),
                user.getCreatedAt()
        );

        for(Post post: user.getPosts()) {
            userDTO.addPost(postMapper.toDTO(post));
        }

        return userDTO;
    }

    public User toUser(UserDTO userDTO) {
        if(userDTO == null) throw new MapperException("Mapper received null UserDTO.");

        User user = new User();
        user.setId(userDTO.getId());
        user.setUserName(userDTO.getUserName());
        user.setEmail(userDTO.getEmail());

        for(PostDTO postDTO: userDTO.getPosts()) {
            Post post = postMapper.toPost(postDTO);
            post.setUser(user);
            user.addPost(post);
        }

        return user;
    }
}
