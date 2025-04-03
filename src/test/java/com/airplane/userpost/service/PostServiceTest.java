package com.airplane.userpost.service;

import com.airplane.userpost.dto.PostDTO;
import com.airplane.userpost.dto.UserDTO;
import com.airplane.userpost.mapper.PostMapper;
import com.airplane.userpost.model.Post;
import com.airplane.userpost.model.User;
import com.airplane.userpost.repository.PostRepository;
import com.airplane.userpost.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class PostServiceTest {

    private PostService postService;
    private PostMapper postMapper;
    private PostRepository postRepository;
    private UserRepository userRepository;

    @BeforeEach
    public void initTest() {
        postMapper = Mockito.mock(PostMapper.class);
        postRepository = Mockito.mock(PostRepository.class);
        userRepository = Mockito.mock(UserRepository.class);
        postService = new PostService(postRepository, userRepository, postMapper);
    }

    @Test
    public void shouldReturnAllPosts() {
        PostDTO postDTO1 = testPostDTO(1L, "title1", "text1", 1L);
        PostDTO postDTO2 = testPostDTO(2L, "title2", "text2", 1L);
        PostDTO postDTO3 = testPostDTO(3L, "title3", "text3", 2L);
        PostDTO postDTO4 = testPostDTO(4L, "title4", "text4", 2L);

        List<PostDTO> expectedPosts = List.of(postDTO1, postDTO2, postDTO3, postDTO4);

        Post post1 = testPost(1L, "title1", "text1");
        Post post2 = testPost(2L, "title2", "text2");
        Post post3 = testPost(3L, "title3", "text3");
        Post post4 = testPost(4L, "title4", "text4");

        User user1 = testUser(1L, "testname1", "testmail1");
        User user2 = testUser(2L, "testname2", "testmail2");

        post1.setUser(user1);
        post2.setUser(user1);
        post3.setUser(user2);
        post4.setUser(user2);

        user1.addPost(post1);
        user1.addPost(post2);
        user2.addPost(post3);
        user2.addPost(post4);

        when(postRepository.findAll()).thenReturn(List.of(post1, post2, post3, post4));
        when(postMapper.toDTO(post1)).thenReturn(postDTO1);
        when(postMapper.toDTO(post2)).thenReturn(postDTO2);
        when(postMapper.toDTO(post3)).thenReturn(postDTO3);
        when(postMapper.toDTO(post4)).thenReturn(postDTO4);

        List<PostDTO> posts = postService.getAllPosts();

        assertEquals(expectedPosts, posts);
    }

    @Test
    public void shouldReturnPostById() {
        PostDTO expectedPost = testPostDTO(1L, "title1", "text1", 1L);

        Post post = testPost(1L, "title1", "text1");
        User user = testUser(1L, "testname1", "testmail1");
        post.setUser(user);
        user.addPost(post);

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(postMapper.toDTO(post)).thenReturn(testPostDTO(1L, "title1", "text1", 1L));

        PostDTO postDTO = postService.getPostById(1L);

        assertEquals(expectedPost, postDTO);
    }

    private Post testPost(Long id, String title, String text) {
        Post post = new Post();
        post.setId(id);
        post.setTitle(title);
        post.setText(text);
        post.setCreatedAt(LocalDateTime.now());

        return post;
    }

    private User testUser(Long userId, String username, String email) {
        User user = new User();
        user.setId(userId);
        user.setUserName(username);
        user.setEmail(email);
        user.setCreatedAt(LocalDateTime.now());

        return user;
    }

    private PostDTO testPostDTO(Long id, String title, String text, Long userId) {
        return new PostDTO(id, title, text, LocalDateTime.now(), userId);
    }
}
