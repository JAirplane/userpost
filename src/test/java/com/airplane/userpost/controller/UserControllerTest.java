package com.airplane.userpost.controller;

import com.airplane.userpost.dto.PostDTO;
import com.airplane.userpost.dto.UserDTO;
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

import static org.hamcrest.Matchers.*;
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
public class UserControllerTest {

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
    public void shouldReturnAllUsers() throws Exception {
        User user1 = testUser(null, "test name1", "test mail1");
        User user2 = testUser(null, "test name2", "test mail2");

        Post post1 = testPost(null, "title1", "text1");
        Post post2 = testPost(null, "title2", "text2");
        Post post3 = testPost(null, "title3", "text3");
        Post post4 = testPost(null, "title4", "text4");

        post1.setUser(user1);
        post2.setUser(user2);
        post3.setUser(user2);
        post4.setUser(user2);

        user1.addPost(post1);
        user2.addPost(post2);
        user2.addPost(post3);
        user2.addPost(post4);

        User savedUser1 = userRepository.save(user1);
        User savedUser2 = userRepository.save(user2);

        Long user1Id = savedUser1.getId();
        Long user2Id = savedUser2.getId();

        mockMvc.perform(get("/users")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(savedUser1.getId()))
                .andExpect(jsonPath("$[0].userName").value(savedUser1.getUserName()))
                .andExpect(jsonPath("$[0].email").value(savedUser1.getEmail()))
                .andExpect(jsonPath("$[0].createdAt").isNotEmpty())
                .andExpect(jsonPath("$[0].posts[*].id").isNotEmpty())
                .andExpect(jsonPath("$[0].posts[*].createdAt").isNotEmpty())
                .andExpect(jsonPath("$[0].posts[*].userId", contains(user1Id.intValue())))
                    .andExpect(jsonPath("$[0].posts[*].title", hasItem("title1")))
                    .andExpect(jsonPath("$[0].posts[*].text", hasItem("text1")))

                .andExpect(jsonPath("$[1].id").value(savedUser2.getId()))
                .andExpect(jsonPath("$[1].userName").value(savedUser2.getUserName()))
                .andExpect(jsonPath("$[1].email").value(savedUser2.getEmail()))
                .andExpect(jsonPath("$[1].createdAt").isNotEmpty())
                .andExpect(jsonPath("$[1].posts[*].userId",
                        contains(savedUser2.getId().intValue(), savedUser2.getId().intValue(), savedUser2.getId().intValue())))
                    .andExpect(jsonPath("$[1].posts[*].title", hasItem("title2")))
                    .andExpect(jsonPath("$[1].posts[*].text", hasItem("text2")))
                    .andExpect(jsonPath("$[1].posts[*].title", hasItem("title3")))
                    .andExpect(jsonPath("$[1].posts[*].text", hasItem("text3")))
                    .andExpect(jsonPath("$[1].posts[*].title", hasItem("title4")))
                    .andExpect(jsonPath("$[1].posts[*].text", hasItem("text4")));

    }

    @Test
    public void shouldReturnUserById() throws Exception {
        User user1 = testUser(null, "test name1", "test mail1");

        Post post1 = testPost(null, "title1", "text1");
        Post post2 = testPost(null, "title2", "text2");

        post1.setUser(user1);
        post2.setUser(user1);
        user1.addPost(post1);
        user1.addPost(post2);

        User savedUser1 = userRepository.save(user1);

        mockMvc.perform(get("/users/{id}", user1.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedUser1.getId()))
                .andExpect(jsonPath("$.userName").value(savedUser1.getUserName()))
                .andExpect(jsonPath("$.email").value(savedUser1.getEmail()))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.posts[*].id").isNotEmpty())
                .andExpect(jsonPath("$.posts[*].createdAt").isNotEmpty())
                .andExpect(jsonPath("$.posts[*].userId", everyItem(is(savedUser1.getId().intValue()))))
                    .andExpect(jsonPath("$.posts[*].title", hasItem("title1")))
                    .andExpect(jsonPath("$.posts[*].text", hasItem("text1")))
                    .andExpect(jsonPath("$.posts[*].title", hasItem("title2")))
                    .andExpect(jsonPath("$.posts[*].text", hasItem("text2")));
    }

    @Test
    public void shouldReturnCreatedUser() throws Exception {
        UserDTO user1 = testUserDTO(null, "test name1", "test mail1");

        PostDTO post1 = testPostDTO(null, "title1", "text1", null);
        PostDTO post2 = testPostDTO(null, "title2", "text2", null);

        user1.addPost(post1);
        user1.addPost(post2);

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.userName").value(user1.getUserName()))
                .andExpect(jsonPath("$.email").value(user1.getEmail()))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.posts[*].id").isNotEmpty())
                .andExpect(jsonPath("$.posts[*].createdAt").isNotEmpty())
                .andExpect(jsonPath("$.posts[*].userId").isNotEmpty())
                    .andExpect(jsonPath("$.posts[*].title", hasItem("title1")))
                    .andExpect(jsonPath("$.posts[*].text", hasItem("text1")))
                    .andExpect(jsonPath("$.posts[*].title", hasItem("title2")))
                    .andExpect(jsonPath("$.posts[*].text", hasItem("text2")));
    }

    @Test
    public void shouldReturnUpdatedUser() throws Exception {
        User user = testUser(null, "test name1", "test mail1");
        User savedUser = userRepository.save(user);

        Post post1 = testPost(null, "title1", "text1");
        Post post2 = testPost(null, "title2", "text2");

        post1.setUser(savedUser);
        post2.setUser(savedUser);
        savedUser.addPost(post1);
        savedUser.addPost(post2);

        Post savedPost1 = postRepository.save(post1);
        Post savedPost2 = postRepository.save(post2);

        UserDTO userArg = testUserDTO(null, "changed name1", "changed mail1");

        PostDTO post1Arg = testPostDTO(savedPost1.getId(), "changed title1", "changed text1", savedUser.getId());
        PostDTO post3 = testPostDTO(null, "title3", "text3", savedUser.getId());

        userArg.addPost(post1Arg);
        userArg.addPost(post3);

        mockMvc.perform(put("/users/{id}", savedUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userArg)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedUser.getId()))
                .andExpect(jsonPath("$.userName").value(userArg.getUserName()))
                .andExpect(jsonPath("$.email").value(userArg.getEmail()))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.posts[*].id").isNotEmpty())
                .andExpect(jsonPath("$.posts[*].createdAt").isNotEmpty())
                .andExpect(jsonPath("$.posts[*].userId", everyItem(is(savedUser.getId().intValue()))))
                .andExpect(jsonPath("$.posts[*].title", hasItem("changed title1")))
                .andExpect(jsonPath("$.posts[*].text", hasItem("changed text1")))
                .andExpect(jsonPath("$.posts[*].title", hasItem("title3")))
                .andExpect(jsonPath("$.posts[*].text", hasItem("text3")));
    }

    @Test
    public void shouldDeleteUser() throws Exception {
        User user = testUser(null, "test name1", "test mail1");
        User savedUser = userRepository.save(user);

        Post post1 = testPost(null, "title1", "text1");
        Post post2 = testPost(null, "title2", "text2");

        post1.setUser(user);
        post2.setUser(user);
        user.addPost(post1);
        user.addPost(post2);

        Post savedPost1 = postRepository.save(post1);
        Post savedPost2 = postRepository.save(post2);

        mockMvc.perform(delete("/users/{id}", savedUser.getId()))
                .andExpect(status().isNoContent());

        boolean isUserInDB = userRepository.existsById(savedUser.getId());
        assertFalse(isUserInDB);

        boolean isPost1InDB = postRepository.existsById(savedPost1.getId());
        assertFalse(isPost1InDB);

        boolean isPost2InDB = postRepository.existsById(savedPost2.getId());
        assertFalse(isPost2InDB);
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

    private UserDTO testUserDTO(Long userId, String username, String email) {
        return new UserDTO(userId, username, email, LocalDateTime.now());
    }
}
