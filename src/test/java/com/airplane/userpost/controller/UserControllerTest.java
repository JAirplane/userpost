package com.airplane.userpost.controller;

import com.airplane.userpost.dto.PostDto;
import com.airplane.userpost.dto.UserDto;
import com.airplane.userpost.model.Post;
import com.airplane.userpost.model.User;
import com.airplane.userpost.repository.PostRepository;
import com.airplane.userpost.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Session;
import org.hibernate.stat.Statistics;
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
import static org.junit.jupiter.api.Assertions.*;
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

    @PersistenceContext
    private EntityManager entityManager;

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
        User user1 = buildUser(null, "test name1", "test mail1");
        User user2 = buildUser(null, "test name2", "test mail2");

        Post post1 = buildPost(null, "title1", "text1");
        Post post2 = buildPost(null, "title2", "text2");
        Post post3 = buildPost(null, "title3", "text3");
        Post post4 = buildPost(null, "title4", "text4");

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
        User user1 = buildUser(null, "test name1", "test mail1");

        Post post1 = buildPost(null, "title1", "text1");
        Post post2 = buildPost(null, "title2", "text2");

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
    public void hibernateNPlus1Test() throws Exception {

        for(int i = 1; i < 6; i++) {
            User user = buildUser(null, "test name" + i, "test mail1" + i);

            for(int j = 1; j < 5; j++) {
                Post post = buildPost(null, "title" + j, "text" + j);
                post.setUser(user);
                user.addPost(post);
            }
            userRepository.save(user);
        }

        Session session = entityManager.unwrap(Session.class);
        Statistics statistics = session.getSessionFactory().getStatistics();
        statistics.setStatisticsEnabled(true);
        statistics.clear();

        mockMvc.perform(get("/users")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        long queries = statistics.getPrepareStatementCount();

        assertTrue(queries <= 2);
    }
	
	@Test
    public void shouldReturnNotFound_userById() throws Exception {

        mockMvc.perform(get("/users/10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.Error").value("User not found with Id: 10"));
    }
	
	@Test
    public void shouldReturnBadRequest_InvalidPathVariable_userById() throws Exception {

        mockMvc.perform(get("/users/abc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.Error").value("Invalid format: abc"));
    }
	
	@Test
    public void shouldReturnBadRequest_UserIdNotPositive_userById() throws Exception {

        mockMvc.perform(get("/users/-1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.userId").value("UserId must be positive number."));
    }

    @Test
    public void shouldReturnCreatedUser() throws Exception {
        UserDto user1 = buildUserDto(null, "test name1", "example@mail.com");

        //Posts won't be saved with new user
        PostDto post1 = buildPostDto(null, "title1", "text1", null);
        PostDto post2 = buildPostDto(null, "title2", "text2", null);

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
                .andExpect(jsonPath("$.posts[*]").isEmpty());
    }
	
	@Test
    public void shouldReturnBadRequest_NullUserDtoFields_newUser() throws Exception {
        UserDto userDtoArg = buildUserDto(null, null, null);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDtoArg)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.userName").value("Username is empty."))
				.andExpect(jsonPath("$.email").value("Email is empty."));
    }
	
	@Test
    public void shouldReturnBadRequest_BadEmail_newUser() throws Exception {
        UserDto userDtoArg = buildUserDto(null, "test username", "bad email");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDtoArg)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").value("Invalid email format."));
    }
	
	@Test
    public void shouldReturnBadRequest_NullUserDto_newUser() throws Exception {

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.Error")
                        .value("Request body is null."));
    }

    @Test
    public void shouldReturnBadRequest_DuplicateFields_newUser() throws Exception {

        UserDto userDtoArg = buildUserDto(null, "test username", "example@mail.com");
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDtoArg)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userName").value("test username"))
                .andExpect(jsonPath("$.email").value("example@mail.com"));

        UserDto userDtoArg2 = buildUserDto(null, "test username", "example@mail.com");
        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDtoArg2)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.Error").value("Unique index or primary key violation."));
    }

    @Test
    public void shouldReturnUpdatedUser() throws Exception {
        User user = buildUser(null, "test name1", "example@mail.com");
        User savedUser = userRepository.save(user);

        Post post1 = buildPost(null, "title1", "text1");
        Post post2 = buildPost(null, "title2", "text2");

        post1.setUser(savedUser);
        post2.setUser(savedUser);
        savedUser.addPost(post1);
        savedUser.addPost(post2);

        Post savedPost1 = postRepository.save(post1);
        Post savedPost2 = postRepository.save(post2);

        UserDto userArg = buildUserDto(null, "changed name1", "changed@mail.com");

        PostDto post1Arg = buildPostDto(savedPost1.getId(), "changed title1", "changed text1", savedUser.getId());
        PostDto post3 = buildPostDto(null, "title3", "text3", savedUser.getId());

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
    public void shouldReturnBadRequest_NullUserDtoFields_updateUser() throws Exception {
		
        UserDto userDtoArg = buildUserDto(null, null, null);

        mockMvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDtoArg)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.userName").value("Username is empty."))
				.andExpect(jsonPath("$.email").value("Email is empty."));
    }
	
	@Test
    public void shouldReturnBadRequest_BadEmail_updateUser() throws Exception {
		
        UserDto userDtoArg = buildUserDto(null, "test username", "bad email");

        mockMvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDtoArg)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").value("Invalid email format."));
    }
	
	@Test
    public void shouldReturnBadRequest_NullUserDto_updateUser() throws Exception {

        mockMvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.Error")
                        .value("Request body is null."));
    }
	
	@Test
    public void shouldReturnNotFound_updateUser() throws Exception {
		
		UserDto userDtoArg = buildUserDto(null, "test username", "example@mail.com");
		
        mockMvc.perform(put("/users/10")
                        .contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(userDtoArg)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.Error").value("User not found with Id: 10"));
    }
	
	@Test
    public void shouldReturnBadRequest_InvalidPathVariable_updateUser() throws Exception {
		
		UserDto userDtoArg = buildUserDto(null, "test username", "example@mail.com");
		
        mockMvc.perform(put("/users/abc")
                        .contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(userDtoArg)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.Error").value("Invalid format: abc"));
    }
	
	@Test
    public void shouldReturnBadRequest_UserIdNotPositive_updateUser() throws Exception {
		
		UserDto userDtoArg = buildUserDto(null, "test username", "example@mail.com");
		
        mockMvc.perform(put("/users/-1")
                        .contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(userDtoArg)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.userId").value("UserId must be positive number."));
    }

    @Test
    public void shouldReturnBadRequest_DuplicateFields_updateUser() throws Exception {

        UserDto userDtoArg = buildUserDto(null, "test username", "example@mail.com");
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDtoArg)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userName").value("test username"))
                .andExpect(jsonPath("$.email").value("example@mail.com"));

        UserDto userDtoArg2 = buildUserDto(null, "other username", "other@mail.com");
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDtoArg2)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userName").value("other username"))
                .andExpect(jsonPath("$.email").value("other@mail.com"));

        var users = userRepository.findAll();
        User user = users.stream()
                .filter(u -> u.getUserName().equals("other username"))
                .findFirst()
                .orElse(null);

        assertNotNull(user);

        UserDto userDtoArgDuplicate = buildUserDto(null, "test username", "example@mail.com");
        mockMvc.perform(put("/users/{id}", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDtoArgDuplicate)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.Error").value("Unique index or primary key violation."));
    }

    @Test
    public void shouldDeleteUser() throws Exception {
        User user = buildUser(null, "test name1", "test mail1");
        User savedUser = userRepository.save(user);

        Post post1 = buildPost(null, "title1", "text1");
        Post post2 = buildPost(null, "title2", "text2");

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
	
	@Test
    public void shouldReturnBadRequest_InvalidPathVariable_deleteUser() throws Exception {
		
        mockMvc.perform(delete("/users/abc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.Error").value("Invalid format: abc"));
    }
	
	@Test
    public void shouldReturnBadRequest_UserIdNotPositive_deleteUser() throws Exception {
		
        mockMvc.perform(delete("/users/-1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.userId").value("UserId must be positive number."));
    }

    private User buildUser(Long userId, String username, String email) {
        User user = new User();
        user.setId(userId);
        user.setUserName(username);
        user.setEmail(email);
        user.setCreatedAt(LocalDateTime.now());

        return user;
    }

    private Post buildPost(Long id, String title, String text) {
        Post post = new Post();
        post.setId(id);
        post.setTitle(title);
        post.setText(text);
        post.setCreatedAt(LocalDateTime.now());

        return post;
    }

    private PostDto buildPostDto
            (Long id, String title, String text, Long userId) {
        return new PostDto(id, title, text, LocalDateTime.now(), userId);
    }

    private UserDto buildUserDto(Long userId, String username, String email) {
        return new UserDto(userId, username, email, LocalDateTime.now());
    }
}
