package com.airplane.userpost.mapper;

import com.airplane.userpost.dto.PostDto;
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
    public void shouldReturnPostDto() {

        PostDto expected = buildPostDto(1L, "test title", "some text", 2L);

        Post post = buildPost(1L, "test title", "some text", 2L);
        PostDto postDto = postMapper.toDto(post);

        assertEquals(expected, postDto);
    }

    @Test
    public void shouldThrowMapperExceptionWhenPostIsNull() {
        Exception exception = assertThrows(MapperException.class,
                () -> postMapper.toDto(null));

        assertEquals("Mapper received null Post.", exception.getMessage());
    }

    @Test
    public void shouldThrowMapperExceptionWhenUserOfPostIsNull() {

        Post post = buildPost(1L, "testTitle", "some text", 2L);
        post.setUser(null);

        Exception exception = assertThrows(MapperException.class,
                () -> postMapper.toDto(post));

        assertEquals("Mapper received Post with null User.", exception.getMessage());
    }

    //ToPost() tests

    @Test
    public void shouldReturnPost() {

        Post expected = buildPost(5L, "Title", "text", 3L);
        expected.setUser(null);

        Post post = postMapper.toPost(buildPostDto(5L, "Title", "text", 3L));

        assertEquals(expected, post);
    }

    @Test
    public void shouldThrowMapperExceptionWhenDtoIsNull() {

        Exception exception = assertThrows(MapperException.class,
                () -> postMapper.toPost(null));

        assertEquals("Mapper received null PostDto.", exception.getMessage());
    }

    private Post buildPost(Long id, String title, String text, Long userId) {
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

    private PostDto buildPostDto(Long id, String title, String text, Long userId) {
        return new PostDto(id, title, text, LocalDateTime.now(), userId);
    }
}
