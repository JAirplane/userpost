package com.airplane.userpost.mapper;

import com.airplane.userpost.dto.PostDto;
import com.airplane.userpost.dto.UserDto;
import com.airplane.userpost.exception.MapperException;
import com.airplane.userpost.model.Post;
import com.airplane.userpost.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

public class UserMapperTest {

    private UserMapper userMapper;
    private PostMapper postMapper;

    @BeforeEach
    public void initTest() {
        postMapper = Mockito.mock(PostMapper.class);
        userMapper = new UserMapper(postMapper);
    }

    //ToUserDTO tests

    @Test
    public void shouldReturnUserDto() {
        User user = buildUser(1L, "testname", "testtext");
        Post post1 = buildPost(1L, "title1", "text1");
        post1.setUser(user);
        Post post2 = buildPost(2L, "title2", "text2");
        post2.setUser(user);
        user.addPost(post1);
        user.addPost(post2);

        UserDto expected = buildUserDto(1L, "testname", "testtext");
        PostDto postDto1 = buildPostDto(1L, "title1", "text1", expected.getId());
        PostDto postDto2 = buildPostDto(2L, "title2", "text2", expected.getId());
        expected.addPost(postDto1);
        expected.addPost(postDto2);

        when(postMapper.toDto(post1)).thenReturn(postDto1);
        when(postMapper.toDto(post2)).thenReturn(postDto2);

        UserDto userDto = userMapper.toDto(user);

        assertEquals(expected, userDto);
    }

    @Test
    public void shouldThrowMapperExceptionWhenNullUserReceived() {
        Exception exception = assertThrows(MapperException.class,
                () -> userMapper.toDto(null));

        assertEquals("Mapper received null User.", exception.getMessage());
    }

    //ToUser tests

    @Test
    public void shouldReturnUser() {
        UserDto userDto = buildUserDto(1L, "testname", "testtext");
        PostDto postDto1 = buildPostDto(1L, "title1", "text1", userDto.getId());
        PostDto postDto2 = buildPostDto(2L, "title2", "text2", userDto.getId());
        userDto.addPost(postDto1);
        userDto.addPost(postDto2);

        User expected = buildUser(1L, "testname", "testtext");
        Post post1 = buildPost(1L, "title1", "text1");
        post1.setUser(expected);
        Post post2 = buildPost(2L, "title2", "text2");
        post2.setUser(expected);
        expected.addPost(post1);
        expected.addPost(post2);

        when(postMapper.toPost(postDto1)).thenReturn(post1);
        when(postMapper.toPost(postDto2)).thenReturn(post2);

        User user = userMapper.toUser(userDto);

        assertEquals(expected, user);
    }

    @Test
    public void shouldThrowMapperExceptionWhenNullUserDto() {
        Exception exception = assertThrows(MapperException.class,
                () -> userMapper.toUser(null));

        assertEquals("Mapper received null UserDTO.", exception.getMessage());
    }

    private User buildUser(Long userId, String username, String email) {
        User user = new User();
        user.setId(userId);
        user.setUserName(username);
        user.setEmail(email);
        user.setCreatedAt(LocalDateTime.now());

        return user;
    }

    private UserDto buildUserDto(Long id, String username, String email) {
        return new UserDto(id, username, email, LocalDateTime.now());
    }

    private Post buildPost(Long id, String title, String text) {
        Post post = new Post();
        post.setId(id);
        post.setTitle(title);
        post.setText(text);
        post.setCreatedAt(LocalDateTime.now());

        return post;
    }

    private PostDto buildPostDto(Long id, String title, String text, Long userId) {
        return new PostDto(id, title, text, LocalDateTime.now(), userId);
    }
}
