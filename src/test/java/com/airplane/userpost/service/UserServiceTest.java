package com.airplane.userpost.service;

import com.airplane.userpost.dto.PostDto;
import com.airplane.userpost.dto.UserDto;
import com.airplane.userpost.exception.UserNotFoundException;
import com.airplane.userpost.mapper.UserMapper;
import com.airplane.userpost.model.Post;
import com.airplane.userpost.model.User;
import com.airplane.userpost.repository.PostRepository;
import com.airplane.userpost.repository.UserRepository;
import jakarta.validation.ConstraintViolationException;
import org.aopalliance.intercept.MethodInterceptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationInterceptor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
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
        UserService service = new UserService(userRepository, postRepository, userMapper);

        var validatorFactory = new LocalValidatorFactoryBean();
        validatorFactory.afterPropertiesSet();

        MethodInterceptor methodValidationInterceptor
                = new MethodValidationInterceptor(validatorFactory.getValidator());

        ProxyFactory proxyFactory = new ProxyFactory(service);
        proxyFactory.addAdvice(methodValidationInterceptor);

        userService = (UserService) proxyFactory.getProxy();
    }

    @Test
    void shouldReturnAllUsers() {
        User user1 = buildUser(1L, "test name1", "test mail1");
        User user2 = buildUser(2L, "test name2", "test mail2");

        Post post1 = buildPost(1L, "title1", "text1");
        Post post2 = buildPost(2L, "title2", "text2");
        Post post3 = buildPost(3L, "title3", "text3");
        Post post4 = buildPost(4L, "title4", "text4");

        post1.setUser(user1);
        post2.setUser(user2);
        post3.setUser(user2);
        post4.setUser(user2);

        user1.addPost(post1);
        user2.addPost(post2);
        user2.addPost(post3);
        user2.addPost(post4);

        UserDto userDto1 = buildUserDto(1L, "test name1", "test mail1");
        UserDto userDto2 = buildUserDto(2L, "test name2", "test mail2");

        PostDto postDto1 = buildPostDto(1L, "title1", "text1", 1L);
        PostDto postDto2 = buildPostDto(2L, "title2", "text2", 2L);
        PostDto postDto3 = buildPostDto(3L, "title3", "text3", 2L);
        PostDto postDto4 = buildPostDto(4L, "title4", "text4", 2L);

        userDto1.addPost(postDto1);
        userDto2.addPost(postDto2);
        userDto2.addPost(postDto3);
        userDto2.addPost(postDto4);

        List<UserDto> expectedUsers = List.of(userDto1, userDto2);

        when(userRepository.findAll()).thenReturn(List.of(user1, user2));
        when(userMapper.toDto(user1)).thenReturn(userDto1);
        when(userMapper.toDto(user2)).thenReturn(userDto2);

        List<UserDto> result = userService.getAllUsers();

        assertEquals(expectedUsers, result);
    }

    @Test
    void shouldReturnUserById() {

        Long userIdArg = 1L;

        User userFromDB = buildUser(userIdArg, "test name", "test mail");
        Post post1 = buildPost(1L, "test title1", "test text1");
        post1.setUser(userFromDB);
        Post post2 = buildPost(2L, "test title2", "test text2");
        post2.setUser(userFromDB);
        userFromDB.addPost(post1);
        userFromDB.addPost(post2);

        UserDto expectedUser = buildUserDto(userIdArg, "test name", "test mail");
        PostDto postDto1 = buildPostDto(1L, "test title1", "test text1", userIdArg);
        PostDto postDto2 = buildPostDto(2L, "test title2", "test text2", userIdArg);
        expectedUser.addPost(postDto1);
        expectedUser.addPost(postDto2);

        UserDto userDtoFromMapper = buildUserDto(userIdArg, "test name", "test mail");
        PostDto postDtoFromMapper1 = buildPostDto(1L, "test title1", "test text1", userIdArg);
        PostDto postDtoFromMapper2 = buildPostDto(2L, "test title2", "test text2", userIdArg);
        userDtoFromMapper.addPost(postDtoFromMapper1);
        userDtoFromMapper.addPost(postDtoFromMapper2);

        when(userRepository.findById(userIdArg)).thenReturn(Optional.of(userFromDB));
        when(userMapper.toDto(userFromDB)).thenReturn(userDtoFromMapper);

        UserDto result = userService.getUserById(userIdArg);

        assertEquals(expectedUser, result);
    }

    @Test
    public void shouldThrowConstraintViolationException_NullArg_getUserById() {

		assertThatThrownBy(() -> userService.getUserById(null))
				.isInstanceOf(ConstraintViolationException.class)
                .satisfies(exception -> {
					var violations = ((ConstraintViolationException) exception).getConstraintViolations();
					assertThat(violations).hasSize(1);
					assertThat(violations)
						.anyMatch(v -> 
							v.getPropertyPath().toString().contains("userId") &&
							v.getMessage().equals("UserId mustn't be null."));
				});
    }
	
	@Test
    public void shouldThrowConstraintViolationException_UserIdNotPositive_getUserById() {

		assertThatThrownBy(() -> userService.getUserById(0L))
				.isInstanceOf(ConstraintViolationException.class)
                .satisfies(exception -> {
					var violations = ((ConstraintViolationException) exception).getConstraintViolations();
					assertThat(violations).hasSize(1);
					assertThat(violations)
						.anyMatch(v -> 
							v.getPropertyPath().toString().contains("userId") &&
							v.getMessage().equals("UserId must be positive number."));
				});
    }

    @Test
    public void shouldThrowUserNotFoundException_getUserById() {

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(UserNotFoundException.class,
                () -> userService.getUserById(1L));

        assertEquals("User not found with Id: 1", exception.getMessage());
    }

    @Test
    void shouldReturnCreatedNewUser() {

        UserDto userDtoArg = buildUserDto(null, "test name", "example@mail.com");

        User userFromMapper = buildUser(null, "test name", "example@mail.com");
        User savedUser = buildUser(1L, "test name", "example@mail.com");

        UserDto userDtoFromMapper = buildUserDto(1L, "test name", "example@mail.com");

        UserDto expectedUserDto = buildUserDto(1L, "test name", "example@mail.com");

        when(userMapper.toUser(userDtoArg)).thenReturn(userFromMapper);
        when(userRepository.save(userFromMapper)).thenReturn(savedUser);
        when(userMapper.toDto(savedUser)).thenReturn(userDtoFromMapper);

        assertEquals(expectedUserDto, userService.createNewUser(userDtoArg));
    }

    @Test
    public void shouldThrowConstraintViolationException_NullUserArg_createNewUser() {

		assertThatThrownBy(() -> userService.createNewUser(null))
				.isInstanceOf(ConstraintViolationException.class)
                .satisfies(exception -> {
					var violations = ((ConstraintViolationException) exception).getConstraintViolations();
					assertThat(violations).hasSize(1);
					assertThat(violations)
						.anyMatch(v -> 
							v.getPropertyPath().toString().contains("userDto") &&
							v.getMessage().equals("UserDto mustn't be null."));
				});
    }

    @Test
    public void shouldThrowConstraintViolationException_NullUserDtoFields_createNewUser() {

		UserDto userDtoArg = buildUserDto(null, null, null);

		assertThatThrownBy(() -> userService.createNewUser(userDtoArg))
				.isInstanceOf(ConstraintViolationException.class)
                .satisfies(exception -> {
					var violations = ((ConstraintViolationException) exception).getConstraintViolations();
					assertThat(violations).hasSize(2);
					assertThat(violations)
						.anyMatch(v -> 
							v.getPropertyPath().toString().contains("userName") &&
							v.getMessage().equals("Username is empty."))
						.anyMatch(v -> 
							v.getPropertyPath().toString().contains("email") &&
							v.getMessage().equals("Email is empty."));
				});
    }
	
	@Test
    public void shouldThrowConstraintViolationException_BadEmail_createNewUser() {

		UserDto userDtoArg = buildUserDto(null, "test name", "bad mail");

		assertThatThrownBy(() -> userService.createNewUser(userDtoArg))
				.isInstanceOf(ConstraintViolationException.class)
                .satisfies(exception -> {
					var violations = ((ConstraintViolationException) exception).getConstraintViolations();
					assertThat(violations).hasSize(1);
					assertThat(violations)
						.anyMatch(v -> 
							v.getPropertyPath().toString().contains("email") &&
							v.getMessage().equals("Invalid email format."));
				});
    }

    @Test
    void shouldReturnUpdatedExistingUser() {

        Long userIdArg = 1L;
        UserDto userDtoArg = buildUserDto(null, "changed name", "changed@mail.com");
        PostDto postDto1 = buildPostDto(1L, "changed title1", "changed text1", null);
        PostDto postDto2 = buildPostDto(null, "test title2", "test text2", null);
        PostDto postDto3 = buildPostDto(null, "test title3", "test text3", null);
        userDtoArg.addPost(postDto1);
        userDtoArg.addPost(postDto2);
        userDtoArg.addPost(postDto3);

        User userFromDB = buildUser(userIdArg, "test name", "example@mail.com");
        Post post = buildPost(8L, "test title4", "test text4");
        post.setUser(userFromDB);
        userFromDB.addPost(post);

        Post existingPost = buildPost(1L, "test title1", "test text1");
        existingPost.setUser(userFromDB);

        User updatedUser = buildUser(userIdArg, "changed name", "changed@mail.com");
        Post post1 = buildPost(1L, "changed title1", "changed text1");
        Post post2 = buildPost(null, "test title2", "test text2");
        Post post3 = buildPost(null, "test title3", "test text3");
        post1.setUser(updatedUser);
        post2.setUser(updatedUser);
        post3.setUser(updatedUser);

        updatedUser.addPost(post1);
        updatedUser.addPost(post2);
        updatedUser.addPost(post3);

        User savedUser = buildUser(userIdArg, "changed name", "changed@mail.com");
        Post updatedPost1 = buildPost(1L, "changed title1", "changed text1");
        Post savedPost2 = buildPost(2L, "test title2", "test text2");
        Post savedPost3 = buildPost(3L, "test title3", "test text3");
        updatedPost1.setUser(savedUser);
        savedPost2.setUser(savedUser);
        savedPost3.setUser(savedUser);

        savedUser.addPost(updatedPost1);
        savedUser.addPost(savedPost2);
        savedUser.addPost(savedPost3);

        UserDto mappedUser = buildUserDto(userIdArg, "changed name", "changed@mail.com");
        PostDto mappedPost1 = buildPostDto(1L, "changed title1", "changed text1", userIdArg);
        PostDto mappedPost2 = buildPostDto(2L, "test title2", "test text2", userIdArg);
        PostDto mappedPost3 = buildPostDto(3L, "test title3", "test text3", userIdArg);

        mappedUser.addPost(mappedPost1);
        mappedUser.addPost(mappedPost2);
        mappedUser.addPost(mappedPost3);

        UserDto expectedUser = buildUserDto(userIdArg, "changed name", "changed@mail.com");
        PostDto expectedPost1 = buildPostDto(1L, "changed title1", "changed text1", userIdArg);
        PostDto expectedPost2 = buildPostDto(2L, "test title2", "test text2", userIdArg);
        PostDto expectedPost3 = buildPostDto(3L, "test title3", "test text3", userIdArg);

        expectedUser.addPost(expectedPost1);
        expectedUser.addPost(expectedPost2);
        expectedUser.addPost(expectedPost3);

        when(userRepository.findById(userIdArg)).thenReturn(Optional.of(userFromDB));
        when(postRepository.findById(1L)).thenReturn(Optional.of(existingPost));
        when(userRepository.save(updatedUser)).thenReturn(savedUser);
        when(userMapper.toDto(savedUser)).thenReturn(mappedUser);

        assertEquals(expectedUser, userService.updateExistingUser(userIdArg, userDtoArg));
    }

    @Test
    public void shouldThrowConstraintViolationException_NullArgs_updateExistingUser() {
		
		assertThatThrownBy(() -> userService.updateExistingUser(null, null))
				.isInstanceOf(ConstraintViolationException.class)
                .satisfies(exception -> {
					var violations = ((ConstraintViolationException) exception).getConstraintViolations();
					assertThat(violations).hasSize(2);
					assertThat(violations)
						.anyMatch(v -> 
							v.getPropertyPath().toString().contains("userId") &&
							v.getMessage().equals("UserId mustn't be null."))
						.anyMatch(v -> 
							v.getPropertyPath().toString().contains("userDto") &&
							v.getMessage().equals("UserDto mustn't be null."));
				});
    }

    @Test
    public void shouldThrowConstraintViolationException_BadUserDtoFields_updateExistingUser() {

        Long userIdArg = 1L;
        UserDto userDtoArg = buildUserDto(null, null, "changed mail");

		assertThatThrownBy(() -> userService.updateExistingUser(userIdArg, userDtoArg))
				.isInstanceOf(ConstraintViolationException.class)
                .satisfies(exception -> {
					var violations = ((ConstraintViolationException) exception).getConstraintViolations();
					assertThat(violations).hasSize(2);
					assertThat(violations)
						.anyMatch(v -> 
							v.getPropertyPath().toString().contains("userName") &&
							v.getMessage().equals("Username is empty."))
						.anyMatch(v -> 
							v.getPropertyPath().toString().contains("email") &&
							v.getMessage().equals("Invalid email format."));
				});
    }

    @Test
    public void shouldThrowConstraintViolation_BadPostField_updateExistingUser() {

        Long userIdArg = 1L;
        UserDto userDtoArg = buildUserDto(null, "username", "example@mail.com");
        PostDto badPost = buildPostDto(null, null, "text", userIdArg);
        userDtoArg.addPost(badPost);

        assertThatThrownBy(() -> userService.updateExistingUser(userIdArg, userDtoArg))
                .isInstanceOf(ConstraintViolationException.class)
                .satisfies(exception -> {
                    var violations = ((ConstraintViolationException) exception).getConstraintViolations();
                    assertThat(violations).hasSize(1);
                    assertThat(violations)
                            .anyMatch(v ->
                                    v.getPropertyPath().toString().contains("title") &&
                                            v.getMessage().equals("Blank post title."));
                });
    }
	
	@Test
    public void shouldThrowConstraintViolation_UserIdNotPositive_updateExistingUser() {

        Long userIdArg = -1L;
        UserDto userDtoArg = buildUserDto(null, "username", "example@mail.com");
        PostDto post = buildPostDto(null, "title", "text", userIdArg);
        userDtoArg.addPost(post);

        assertThatThrownBy(() -> userService.updateExistingUser(userIdArg, userDtoArg))
                .isInstanceOf(ConstraintViolationException.class)
                .satisfies(exception -> {
                    var violations = ((ConstraintViolationException) exception).getConstraintViolations();
                    assertThat(violations).hasSize(1);
                    assertThat(violations)
                            .anyMatch(v ->
                                    v.getPropertyPath().toString().contains("userId") &&
                                            v.getMessage().equals("UserId must be positive number."));
                });
    }

    @Test
    public void shouldThrowUserNotFoundException_updateExistingUser() {

        Long userIdArg = 1L;
        UserDto userDtoArg = buildUserDto(null, "test name", "example@mail.com");

        Exception exception = assertThrows(UserNotFoundException.class,
                () -> userService.updateExistingUser(userIdArg, userDtoArg));

        assertEquals("User not found with Id: 1", exception.getMessage());

    }

    @Test
    public void deleteUser() {

        userService.deleteUser(1L);

        verify(userRepository).deleteById(1L);
    }
	
	@Test
    public void shouldThrowConstraintViolationException_NullUserIdArg_deleteUser() {

		assertThatThrownBy(() -> userService.deleteUser(null))
				.isInstanceOf(ConstraintViolationException.class)
                .satisfies(exception -> {
					var violations = ((ConstraintViolationException) exception).getConstraintViolations();
					assertThat(violations).hasSize(1);
					assertThat(violations)
						.anyMatch(v -> 
							v.getPropertyPath().toString().contains("userId") &&
							v.getMessage().equals("UserId mustn't be null."));
				});
    }
	
	@Test
    public void shouldThrowConstraintViolationException_UserIdNotPositive_deleteUser() {

		assertThatThrownBy(() -> userService.deleteUser(-1L))
				.isInstanceOf(ConstraintViolationException.class)
                .satisfies(exception -> {
					var violations = ((ConstraintViolationException) exception).getConstraintViolations();
					assertThat(violations).hasSize(1);
					assertThat(violations)
						.anyMatch(v -> 
							v.getPropertyPath().toString().contains("userId") &&
							v.getMessage().equals("UserId must be positive number."));
				});
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

    private User buildUser(Long userId, String username, String email) {
        User user = new User();
        user.setId(userId);
        user.setUserName(username);
        user.setEmail(email);
        user.setCreatedAt(LocalDateTime.now());

        return user;
    }

    private UserDto buildUserDto(Long userId, String username, String email) {
        return new UserDto(userId, username, email, LocalDateTime.now());
    }
}
