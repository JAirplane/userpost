package com.airplane.userpost.mapper;

import com.airplane.userpost.dto.PostDto;
import com.airplane.userpost.dto.UserDto;
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

    public UserDto toDto(User user) {
        if(user == null) throw new MapperException("Mapper received null User.");

        UserDto userDto = new UserDto(
                user.getId(),
                user.getUserName(),
                user.getEmail(),
                user.getCreatedAt()
        );

        for(Post post: user.getPosts()) {
            userDto.addPost(postMapper.toDto(post));
        }

        return userDto;
    }

    public User toUser(UserDto userDto) {
        if(userDto == null) throw new MapperException("Mapper received null UserDTO.");

        User user = new User();
        user.setId(userDto.getId());
        user.setUserName(userDto.getUserName());
        user.setEmail(userDto.getEmail());

        for(PostDto postDTO: userDto.getPosts()) {
            Post post = postMapper.toPost(postDTO);
            post.setUser(user);
            user.addPost(post);
        }

        return user;
    }
}
