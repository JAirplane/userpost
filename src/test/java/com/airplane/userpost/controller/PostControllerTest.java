package com.airplane.userpost.controller;

import com.airplane.userpost.dto.PostDTO;
import com.airplane.userpost.model.Post;
import com.airplane.userpost.model.User;
import com.airplane.userpost.repository.PostRepository;
import com.airplane.userpost.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
public class PostControllerTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void initTest() {
        postRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    public void shouldReturnAllPosts() throws Exception {

        User user = userRepository.save(testUser(null, "test name", "test mail"));
        Post post1 = testPost(null, "test title1", "test text1");
        post1.setUser(user);
        Post post2 = testPost(null, "test title2", "test text2");
        post2.setUser(user);
        postRepository.save(post1);
        postRepository.save(post2);

        mockMvc.perform(get("/posts")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(post1.getId()))
                .andExpect(jsonPath("$[0].title").value(post1.getTitle()))
                .andExpect(jsonPath("$[0].text").value(post1.getText()))
                .andExpect(jsonPath("$[0].createdAt").isNotEmpty())
                .andExpect(jsonPath("$[0].userId").value(post1.getUser().getId()))
                .andExpect(jsonPath("$[1].id").value(post2.getId()))
                .andExpect(jsonPath("$[1].title").value(post2.getTitle()))
                .andExpect(jsonPath("$[1].text").value(post2.getText()))
                .andExpect(jsonPath("$[1].createdAt").isNotEmpty())
                .andExpect(jsonPath("$[1].userId").value(post2.getUser().getId()));
    }

    @Test
    public void shouldReturnPostById() throws Exception {
        User user = userRepository.save(testUser(null, "test name", "test mail"));
        Post post1 = testPost(null, "test title1", "test text1");
        post1.setUser(user);
        Post savedPost = postRepository.save(post1);

        mockMvc.perform(get("/posts/{id}", savedPost.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedPost.getId()))
                .andExpect(jsonPath("$.title").value(savedPost.getTitle()))
                .andExpect(jsonPath("$.text").value(savedPost.getText()))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.userId").value(savedPost.getUser().getId()));
    }

    @Test
    public void shouldReturnNotFound_PostById() throws Exception {

        mockMvc.perform(get("/posts/{id}", -1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.Error").value("Post not found for Id: -1"));
    }

    @Test
    public void shouldReturnBadRequest_PostById() throws Exception {

        mockMvc.perform(get("/posts/abc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.Error").value("Invalid ID format: abc"));
    }

    @Test
    public void shouldReturnCreatedPostDTO() throws Exception {
        User user = userRepository.save(testUser(null, "test name", "example@mail.com"));
        PostDTO postDTOArg = testPostDTO(null, "test title", "test text", null);

        mockMvc.perform(post("/posts/{userId}", user.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(postDTOArg)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.title").value(postDTOArg.title()))
                .andExpect(jsonPath("$.text").value(postDTOArg.text()))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.userId").value(user.getId()));
    }

    @Test
    public void shouldReturnBadRequest_CreateNewPost_BadArgs() throws Exception {
        PostDTO postDTOArg = testPostDTO(null, null, "test text", null);

        mockMvc.perform(post("/posts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postDTOArg)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title")
                        .value("Blank post title."));
    }

    @Test
    public void shouldReturnBadRequest_CreateNewPost_NullPostArg() throws Exception {

        mockMvc.perform(post("/posts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.Error")
                        .value("Request body is null."));
    }

    @Test
    public void shouldReturnBadRequest_CreateNewPost_BadUserId() throws Exception {

        mockMvc.perform(post("/posts/abc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.Error")
                        .value("Invalid ID format: abc"));
    }

    @Test
    public void shouldReturnUpdatedPostDTO() throws Exception {
        User user = userRepository.save(testUser(null, "test name", "example@mail.com"));
        Post post = testPost(null, "test title", "test text");
        post.setUser(user);
        Post savedPost = postRepository.save(post);
        PostDTO postDTOArg = testPostDTO(null, "changed title", "changed text", null);

        mockMvc.perform(put("/posts/{postId}", savedPost.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(postDTOArg)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedPost.getId()))
                .andExpect(jsonPath("$.title").value(postDTOArg.title()))
                .andExpect(jsonPath("$.text").value(postDTOArg.text()))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.userId").value(user.getId()));
    }

    @Test
    public void shouldReturnBadRequest_UpdatePost_NullPostArg() throws Exception {

        mockMvc.perform(put("/posts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.Error")
                        .value("Request body is null."));
    }

    @Test
    public void shouldReturnBadRequest_UpdatePost_BadArgs() throws Exception {
        PostDTO postDTOArg = testPostDTO(null, null, "test text", null);

        mockMvc.perform(put("/posts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postDTOArg)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title")
                        .value("Blank post title."));
    }

    @Test
    public void shouldReturnBadRequest_UpdatePost_BadPostId() throws Exception {
        PostDTO postDTOArg = testPostDTO(null, "test title", "test text", null);

        mockMvc.perform(put("/posts/abc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postDTOArg)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.Error")
                        .value("Invalid ID format: abc"));
    }

    @Test
    public void shouldDeletePost() throws Exception {
        User user = userRepository.save(testUser(null, "test name", "test mail"));
        Post post = testPost(null, "test title", "test text");
        post.setUser(user);
        Post savedPost = postRepository.save(post);

        mockMvc.perform(delete("/posts/{postId}", savedPost.getId()))
                .andExpect(status().isNoContent());

        boolean isPostStillThere = postRepository.findById(savedPost.getId()).isPresent();

        assertFalse(isPostStillThere);
    }

    @Test
    public void shouldReturnBadRequest_DeletePost_BadPostId() throws Exception {

        mockMvc.perform(delete("/posts/abc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.Error")
                        .value("Invalid ID format: abc"));
    }

    private User testUser(Long userId, String username, String email) {
        User user = new User();
        user.setId(userId);
        user.setUserName(username);
        user.setEmail(email);
        user.setCreatedAt(LocalDateTime.now());

        return user;
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
