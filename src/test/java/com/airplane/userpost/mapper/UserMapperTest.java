package com.airplane.userpost.mapper;

import com.airplane.userpost.dto.PostDTO;
import com.airplane.userpost.dto.UserDTO;
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
    public void shouldReturnUserDTO() {
        User user = testUser(1L, "testname", "testtext");
        Post post1 = testPost(1L, "title1", "text1");
        post1.setUser(user);
        Post post2 = testPost(2L, "title2", "text2");
        post2.setUser(user);
        user.addPost(post1);
        user.addPost(post2);

        UserDTO expected = testUserDTO(1L, "testname", "testtext");
        PostDTO postDTO1 = testPostDTO(1L, "title1", "text1", expected.getId());
        PostDTO postDTO2 = testPostDTO(2L, "title2", "text2", expected.getId());
        expected.addPost(postDTO1);
        expected.addPost(postDTO2);

        when(postMapper.toDTO(post1)).thenReturn(postDTO1);
        when(postMapper.toDTO(post2)).thenReturn(postDTO2);

        UserDTO userDTO = userMapper.toDTO(user);

        assertEquals(expected, userDTO);
    }

    @Test
    public void shouldThrowMapperExceptionWhenNullUserReceived() {
        Exception exception = assertThrows(MapperException.class,
                () -> userMapper.toDTO(null));

        assertEquals("Mapper received null User.", exception.getMessage());
    }

    //ToUser tests

    @Test
    public void shouldReturnUser() {
        UserDTO userDTO = testUserDTO(1L, "testname", "testtext");
        PostDTO postDTO1 = testPostDTO(1L, "title1", "text1", userDTO.getId());
        PostDTO postDTO2 = testPostDTO(2L, "title2", "text2", userDTO.getId());
        userDTO.addPost(postDTO1);
        userDTO.addPost(postDTO2);

        User expected = testUser(1L, "testname", "testtext");
        Post post1 = testPost(1L, "title1", "text1");
        post1.setUser(expected);
        Post post2 = testPost(2L, "title2", "text2");
        post2.setUser(expected);
        expected.addPost(post1);
        expected.addPost(post2);

        when(postMapper.toPost(postDTO1)).thenReturn(post1);
        when(postMapper.toPost(postDTO2)).thenReturn(post2);

        User user = userMapper.toUser(userDTO);

        assertEquals(expected, user);
    }

    @Test
    public void shouldThrowMapperExceptionWhenNullUserDTO() {
        Exception exception = assertThrows(MapperException.class,
                () -> userMapper.toUser(null));

        assertEquals("Mapper received null UserDTO.", exception.getMessage());
    }

    private User testUser(Long userId, String username, String email) {
        User user = new User();
        user.setId(userId);
        user.setUserName(username);
        user.setEmail(email);
        user.setCreatedAt(LocalDateTime.now());

        return user;
    }

    private UserDTO testUserDTO(Long id, String username, String email) {
        return new UserDTO(id, username, email, LocalDateTime.now());
    }

    private Post testPost(Long id, String title, String text) {
        Post post = new Post();
        post.setId(id);
        post.setTitle(title);
        post.setText(text);
        post.setCreatedAt(LocalDateTime.now());

        return post;
    }

    private PostDTO testPostDTO(Long id, String title, String text, Long userId) {
        return new PostDTO(id, title, text, LocalDateTime.now(), userId);
    }
}
