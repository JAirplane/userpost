package com.airplane.userpost.service;

import com.airplane.userpost.dto.PostDTO;
import com.airplane.userpost.dto.UserDTO;
import com.airplane.userpost.exception.UserNotFoundException;
import com.airplane.userpost.mapper.UserMapper;
import com.airplane.userpost.model.Post;
import com.airplane.userpost.model.User;
import com.airplane.userpost.repository.PostRepository;
import com.airplane.userpost.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UserServiceTest {

    private UserService userService;
    private UserRepository userRepository;
    private PostRepository postRepository;
    private UserMapper userMapper;

    @BeforeEach
    public void initTest() {

        userMapper = Mockito.mock(UserMapper.class);
        postRepository = Mockito.mock(PostRepository.class);
        userRepository = Mockito.mock(UserRepository.class);
        userService = new UserService(userRepository, postRepository, userMapper);
    }

    @Test
    void shouldReturnAllUsers() {
        User user1 = testUser(1L, "test name1", "test mail1");
        User user2 = testUser(2L, "test name2", "test mail2");

        Post post1 = testPost(1L, "title1", "text1");
        Post post2 = testPost(2L, "title2", "text2");
        Post post3 = testPost(3L, "title3", "text3");
        Post post4 = testPost(4L, "title4", "text4");

        post1.setUser(user1);
        post2.setUser(user2);
        post3.setUser(user2);
        post4.setUser(user2);

        user1.addPost(post1);
        user2.addPost(post2);
        user2.addPost(post3);
        user2.addPost(post4);

        UserDTO userDTO1 = testUserDTO(1L, "test name1", "test mail1");
        UserDTO userDTO2 = testUserDTO(2L, "test name2", "test mail2");

        PostDTO postDTO1 = testPostDTO(1L, "title1", "text1", 1L);
        PostDTO postDTO2 = testPostDTO(2L, "title2", "text2", 2L);
        PostDTO postDTO3 = testPostDTO(3L, "title3", "text3", 2L);
        PostDTO postDTO4 = testPostDTO(4L, "title4", "text4", 2L);

        userDTO1.addPost(postDTO1);
        userDTO2.addPost(postDTO2);
        userDTO2.addPost(postDTO3);
        userDTO2.addPost(postDTO4);

        List<UserDTO> expectedUsers = List.of(userDTO1, userDTO2);

        when(userRepository.findAll()).thenReturn(List.of(user1, user2));
        when(userMapper.toDTO(user1)).thenReturn(userDTO1);
        when(userMapper.toDTO(user2)).thenReturn(userDTO2);

        List<UserDTO> result = userService.getAllUsers();

        assertEquals(expectedUsers, result);
    }

    @Test
    void shouldReturnUserById() {

        Long userIdArg = 1L;

        User userFromDB = testUser(userIdArg, "test name", "test mail");
        Post post1 = testPost(1L, "test title1", "test text1");
        post1.setUser(userFromDB);
        Post post2 = testPost(2L, "test title2", "test text2");
        post2.setUser(userFromDB);
        userFromDB.addPost(post1);
        userFromDB.addPost(post2);

        UserDTO expectedUser = testUserDTO(userIdArg, "test name", "test mail");
        PostDTO postDTO1 = testPostDTO(1L, "test title1", "test text1", userIdArg);
        PostDTO postDTO2 = testPostDTO(2L, "test title2", "test text2", userIdArg);
        expectedUser.addPost(postDTO1);
        expectedUser.addPost(postDTO2);

        UserDTO userDTOFromMapper = testUserDTO(userIdArg, "test name", "test mail");
        PostDTO postDTOFromMapper1 = testPostDTO(1L, "test title1", "test text1", userIdArg);
        PostDTO postDTOFromMapper2 = testPostDTO(2L, "test title2", "test text2", userIdArg);
        userDTOFromMapper.addPost(postDTOFromMapper1);
        userDTOFromMapper.addPost(postDTOFromMapper2);

        when(userRepository.findById(userIdArg)).thenReturn(Optional.of(userFromDB));
        when(userMapper.toDTO(userFromDB)).thenReturn(userDTOFromMapper);

        UserDTO result = userService.getUserById(userIdArg);

        assertEquals(expectedUser, result);
    }

    @Test
    public void shouldThrowIllegalArgumentExceptionUserById() {

        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> userService.getUserById(null));

        assertEquals("Getting User by Id failed: null Id received", exception.getMessage());
    }

    @Test
    public void shouldThrowUserNotFoundExceptionUserById() {

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(UserNotFoundException.class,
                () -> userService.getUserById(1L));

        assertEquals("User not found with Id: 1", exception.getMessage());
    }

    @Test
    void shouldReturnCreatedNewUser() {

        UserDTO userDTOArg = testUserDTO(null, "test name", "test mail");

        User userFromMapper = testUser(null, "test name", "test mail");
        User savedUser = testUser(1L, "test name", "test mail");

        UserDTO userDTOFromMapper = testUserDTO(1L, "test name", "test mail");

        UserDTO expectedUserDTO = testUserDTO(1L, "test name", "test mail");

        when(userMapper.toUser(userDTOArg)).thenReturn(userFromMapper);
        when(userRepository.save(userFromMapper)).thenReturn(savedUser);
        when(userMapper.toDTO(savedUser)).thenReturn(userDTOFromMapper);

        assertEquals(expectedUserDTO, userService.createNewUser(userDTOArg));
    }

    @Test
    public void shouldThrowIllegalArgumentExceptionWhenCreatingUser() {

        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> userService.createNewUser(null));

        assertEquals("User creation failed: null DTO received", exception.getMessage());
    }

    @Test
    public void shouldThrowIllegalArgumentExceptionBadFieldsWhenCreatingUser() {

        UserDTO userDTOArg_withId = testUserDTO(1L, "test name", "test mail");
        UserDTO userDTOArg_nullUsername = testUserDTO(null, null, "test mail");
        UserDTO userDTOArg_nullEmail = testUserDTO(null, "test name", null);

        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> userService.createNewUser(userDTOArg_withId));

        assertEquals("User creation failed: bad DTO field", exception.getMessage());

        exception = assertThrows(IllegalArgumentException.class,
                () -> userService.createNewUser(userDTOArg_nullUsername));

        assertEquals("User creation failed: bad DTO field", exception.getMessage());

        exception = assertThrows(IllegalArgumentException.class,
                () -> userService.createNewUser(userDTOArg_nullEmail));

        assertEquals("User creation failed: bad DTO field", exception.getMessage());
    }

    @Test
    void shouldReturnUpdatedExistingUser() {

        Long userIdArg = 1L;
        UserDTO userDTOArg = testUserDTO(null, "changed name", "changed mail");
        PostDTO postDTO1 = testPostDTO(1L, "changed title1", "changed text1", null);
        PostDTO postDTO2 = testPostDTO(null, "test title2", "test text2", null);
        PostDTO postDTO3 = testPostDTO(null, "test title3", "test text3", null);
        userDTOArg.addPost(postDTO1);
        userDTOArg.addPost(postDTO2);
        userDTOArg.addPost(postDTO3);

        User userFromDB = testUser(userIdArg, "test name", "test mail");
        Post post = testPost(8L, "test title4", "test mail4");
        post.setUser(userFromDB);
        userFromDB.addPost(post);

        Post existingPost = testPost(1L, "test title1", "test mail1");
        existingPost.setUser(userFromDB);

        User updatedUser = testUser(userIdArg, "changed name", "changed mail");
        Post post1 = testPost(1L, "changed title1", "changed text1");
        Post post2 = testPost(null, "test title2", "test text2");
        Post post3 = testPost(null, "test title3", "test text3");
        post1.setUser(updatedUser);
        post2.setUser(updatedUser);
        post3.setUser(updatedUser);

        updatedUser.addPost(post1);
        updatedUser.addPost(post2);
        updatedUser.addPost(post3);

        User savedUser = testUser(userIdArg, "changed name", "changed mail");
        Post updatedPost1 = testPost(1L, "changed title1", "changed text1");
        Post savedPost2 = testPost(2L, "test title2", "test text2");
        Post savedPost3 = testPost(3L, "test title3", "test text3");
        updatedPost1.setUser(savedUser);
        savedPost2.setUser(savedUser);
        savedPost3.setUser(savedUser);

        savedUser.addPost(updatedPost1);
        savedUser.addPost(savedPost2);
        savedUser.addPost(savedPost3);

        UserDTO mappedUser = testUserDTO(userIdArg, "changed name", "changed mail");
        PostDTO mappedPost1 = testPostDTO(1L, "changed title1", "changed text1", userIdArg);
        PostDTO mappedPost2 = testPostDTO(2L, "test title2", "test text2", userIdArg);
        PostDTO mappedPost3 = testPostDTO(3L, "test title3", "test text3", userIdArg);

        mappedUser.addPost(mappedPost1);
        mappedUser.addPost(mappedPost2);
        mappedUser.addPost(mappedPost3);

        UserDTO expectedUser = testUserDTO(userIdArg, "changed name", "changed mail");
        PostDTO expectedPost1 = testPostDTO(1L, "changed title1", "changed text1", userIdArg);
        PostDTO expectedPost2 = testPostDTO(2L, "test title2", "test text2", userIdArg);
        PostDTO expectedPost3 = testPostDTO(3L, "test title3", "test text3", userIdArg);

        expectedUser.addPost(expectedPost1);
        expectedUser.addPost(expectedPost2);
        expectedUser.addPost(expectedPost3);

        when(userRepository.findById(userIdArg)).thenReturn(Optional.of(userFromDB));
        when(postRepository.findById(1L)).thenReturn(Optional.of(existingPost));
        when(userRepository.save(updatedUser)).thenReturn(savedUser);
        when(userMapper.toDTO(savedUser)).thenReturn(mappedUser);

        assertEquals(expectedUser, userService.updateExistingUser(userIdArg, userDTOArg));
    }

    @Test
    public void shouldThrowIllegalArgumentExceptionWhenUpdatingUser() {

        Long userIdArg = 1L;
        UserDTO userDTOArg = testUserDTO(null, "changed name", "changed mail");

        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> userService.updateExistingUser(null, userDTOArg));

        assertEquals("User update failed: received null arg", exception.getMessage());

        exception = assertThrows(IllegalArgumentException.class,
                () -> userService.updateExistingUser(userIdArg, null));

        assertEquals("User update failed: received null arg", exception.getMessage());
    }

    @Test
    public void shouldThrowIllegalArgumentExceptionWhenUpdatingUser_BadFields() {

        Long userIdArg = 1L;
        UserDTO userDTOArg_nullUsername = testUserDTO(null, null, "changed mail");
        UserDTO userDTOArg_nullEmail = testUserDTO(null, "test username", null);

        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> userService.updateExistingUser(userIdArg, userDTOArg_nullUsername));

        assertEquals("User creation failed: bad DTO field", exception.getMessage());

        exception = assertThrows(IllegalArgumentException.class,
                () -> userService.updateExistingUser(userIdArg, userDTOArg_nullEmail));

        assertEquals("User creation failed: bad DTO field", exception.getMessage());
    }

    @Test
    public void shouldThrowUserNotFoundExceptionWhenUpdatingUser() {

        Long userIdArg = 1L;
        UserDTO userDTOArg = testUserDTO(null, "test name", "changed mail");

        Exception exception = assertThrows(UserNotFoundException.class,
                () -> userService.updateExistingUser(userIdArg, userDTOArg));

        assertEquals("User update failed: user not found with Id: 1", exception.getMessage());

    }

    @Test
    void deleteUser() {

        userService.deleteUser(1L);

        verify(userRepository).deleteById(1L);
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

    private User testUser(Long userId, String username, String email) {
        User user = new User();
        user.setId(userId);
        user.setUserName(username);
        user.setEmail(email);
        user.setCreatedAt(LocalDateTime.now());

        return user;
    }

    private UserDTO testUserDTO(Long userId, String username, String email) {
        return new UserDTO(userId, username, email, LocalDateTime.now());
    }
}
