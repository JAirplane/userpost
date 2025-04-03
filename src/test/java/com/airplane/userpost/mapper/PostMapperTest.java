package com.airplane.userpost.mapper;

import com.airplane.userpost.dto.PostDTO;
import com.airplane.userpost.exception.MapperException;
import com.airplane.userpost.model.Post;
import com.airplane.userpost.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class PostMapperTest {

    private PostMapper postMapper;

    @BeforeEach
    public void initTest() {
        postMapper = new PostMapper();
    }

    //ToDTO() tests

    @Test
    public void shouldReturnPostDTO() {

        PostDTO expected = testPostDTO(1L, "test title", "some text", 2L);

        Post post = testPost(1L, "test title", "some text", 2L);
        PostDTO postDTO = postMapper.toDTO(post);

        assertEquals(expected, postDTO);
    }

    @Test
    public void shouldThrowMapperExceptionWhenPostIsNull() {
        Exception exception = assertThrows(MapperException.class,
                () -> postMapper.toDTO(null));

        assertEquals("Mapper received null Post.", exception.getMessage());
    }

    @Test
    public void shouldThrowMapperExceptionWhenUserOfPostIsNull() {

        Post post = testPost(1L, "testTitle", "some text", 2L);
        post.setUser(null);

        Exception exception = assertThrows(MapperException.class,
                () -> postMapper.toDTO(post));

        assertEquals("Mapper received Post with null User.", exception.getMessage());
    }

    //ToPost() tests

    @Test
    public void shouldReturnPost() {

        Post expected = testPost(5L, "Title", "text", 3L);
        expected.setUser(null);

        Post post = postMapper.toPost(testPostDTO(5L, "Title", "text", 3L));

        assertEquals(expected, post);
    }

    @Test
    public void shouldReturnMapperExceptionWhenDTOIsNull() {

        Exception exception = assertThrows(MapperException.class,
                () -> postMapper.toPost(null));

        assertEquals("Mapper received null PostDTO.", exception.getMessage());
    }

    private Post testPost(Long id, String title, String text, Long userId) {
        Post post = new Post();
        post.setId(id);
        post.setTitle(title);
        post.setText(text);
        post.setCreatedAt(LocalDateTime.now());

        //only user id involved in DTO creation
        User user = new User();
        user.setId(userId);
        user.addPost(post);

        post.setUser(user);

        return post;
    }

    private PostDTO testPostDTO(Long id, String title, String text, Long userId) {
        return new PostDTO(id, title, text, LocalDateTime.now(), userId);
    }
}
