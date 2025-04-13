package com.airplane.userpost.service;

import com.airplane.userpost.dto.PostDto;
import com.airplane.userpost.exception.PostNotFoundException;
import com.airplane.userpost.exception.UserNotFoundException;
import com.airplane.userpost.mapper.PostMapper;
import com.airplane.userpost.model.Post;
import com.airplane.userpost.model.User;
import com.airplane.userpost.repository.PostRepository;
import com.airplane.userpost.repository.UserRepository;
import jakarta.validation.ConstraintViolationException;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.framework.ProxyFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationInterceptor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

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
        PostService service = new PostService(postRepository, userRepository, postMapper);

        var validatorFactory = new LocalValidatorFactoryBean();
        validatorFactory.afterPropertiesSet();

        MethodInterceptor methodValidationInterceptor
                = new MethodValidationInterceptor(validatorFactory.getValidator());

        ProxyFactory proxyFactory = new ProxyFactory(service);
        proxyFactory.addAdvice(methodValidationInterceptor);

        postService = (PostService) proxyFactory.getProxy();
    }

    @Test
    public void shouldReturnAllPosts() {
        PostDto postDto1 = buildPostDto(1L, "title1", "text1", 1L);
        PostDto postDto2 = buildPostDto(2L, "title2", "text2", 1L);
        PostDto postDto3 = buildPostDto(3L, "title3", "text3", 2L);
        PostDto postDto4 = buildPostDto(4L, "title4", "text4", 2L);

        List<PostDto> expectedPosts = List.of(postDto1, postDto2, postDto3, postDto4);

        Post post1 = buildPost(1L, "title1", "text1");
        Post post2 = buildPost(2L, "title2", "text2");
        Post post3 = buildPost(3L, "title3", "text3");
        Post post4 = buildPost(4L, "title4", "text4");

        User user1 = buildUser(1L, "testname1", "testmail1");
        User user2 = buildUser(2L, "testname2", "testmail2");

        post1.setUser(user1);
        post2.setUser(user1);
        post3.setUser(user2);
        post4.setUser(user2);

        user1.addPost(post1);
        user1.addPost(post2);
        user2.addPost(post3);
        user2.addPost(post4);

        when(postRepository.findAll()).thenReturn(List.of(post1, post2, post3, post4));
        when(postMapper.toDto(post1)).thenReturn(postDto1);
        when(postMapper.toDto(post2)).thenReturn(postDto2);
        when(postMapper.toDto(post3)).thenReturn(postDto3);
        when(postMapper.toDto(post4)).thenReturn(postDto4);

        List<PostDto> posts = postService.getAllPosts();

        assertEquals(expectedPosts, posts);
    }

    @Test
    public void shouldReturnPostDTO_getPostById() {
        PostDto expectedPost = buildPostDto(1L, "title1", "text1", 1L);

        Post post = buildPost(1L, "title1", "text1");
        User user = buildUser(1L, "testname1", "example@mail.com");
        post.setUser(user);
        user.addPost(post);

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(postMapper.toDto(post)).thenReturn(buildPostDto(1L, "title1", "text1", 1L));

        PostDto postDto = postService.getPostById(1L);

        assertEquals(expectedPost, postDto);
    }

    @Test
    public void shouldThrowConstraintViolationExceptionWhenPostIdIsNull_getPostById() {

        assertThatThrownBy(() -> postService.getPostById(null))
				.isInstanceOf(ConstraintViolationException.class)
                .satisfies(exception -> {
					var violations = ((ConstraintViolationException) exception).getConstraintViolations();
					assertThat(violations).hasSize(1);
					assertThat(violations)
						.anyMatch(v -> 
							v.getPropertyPath().toString().contains("postId") &&
							v.getMessage().equals("PostId mustn't be null."));
				});
    }
	
	@Test
    public void shouldThrowConstraintViolationExceptionWhenPostIdIsNotPositive_getPostById() {

        assertThatThrownBy(() -> postService.getPostById(0L))
				.isInstanceOf(ConstraintViolationException.class)
                .satisfies(exception -> {
					var violations = ((ConstraintViolationException) exception).getConstraintViolations();
					assertThat(violations).hasSize(1);
					assertThat(violations)
						.anyMatch(v -> 
							v.getPropertyPath().toString().contains("postId") &&
							v.getMessage().equals("PostId must be positive number."));
				});
    }

    @Test
    public void shouldThrowPostNotFoundExceptionWhenPostNotFoundInDB_getPostById() {

        when(postRepository.findById(100L)).thenReturn(Optional.empty());
        Exception exception = assertThrows(PostNotFoundException.class,
                () -> postService.getPostById(100L));

        assertEquals("Post not found for Id: 100", exception.getMessage());
    }

    @Test
    public void shouldReturnCreatedPostDto() {
        Long userIdArg = 1L;
        PostDto postDtoArg = buildPostDto(11L, "test title", "some text", null);

        User user = buildUser(userIdArg, "test name", "test mail");

        Post postFromMapper = buildPost(null, "test title", "some text");
        postFromMapper.setUser(user);

        Post savedPost = buildPost(2L, "test title", "some text");
        savedPost.setUser(user);

        PostDto postDtoFromMapper = buildPostDto(2L, "test title", "some text", user.getId());

        PostDto expected = buildPostDto(2L, "test title", "some text", userIdArg);

        when(userRepository.findById(userIdArg)).thenReturn(Optional.of(user));
        when(postMapper.toPost(postDtoArg)).thenReturn(postFromMapper);
        when(postRepository.save(postFromMapper)).thenReturn(savedPost);
        when(postMapper.toDto(savedPost)).thenReturn(postDtoFromMapper);

        PostDto result = postService.createNewPost(userIdArg, postDtoArg);

        assertEquals(expected, result);
    }

    @Test
    public void shouldThrowConstraintViolationExceptionWhenNullArguments_createNewPost() {

		assertThatThrownBy(() -> postService.createNewPost(null, null))
				.isInstanceOf(ConstraintViolationException.class)
                .satisfies(exception -> {
					var violations = ((ConstraintViolationException) exception).getConstraintViolations();
					assertThat(violations).hasSize(2);
					assertThat(violations)
						.anyMatch(v -> 
							v.getPropertyPath().toString().contains("userId") &&
							v.getMessage().equals("UserId mustn't be null."))
						.anyMatch(v -> 
							v.getPropertyPath().toString().contains("postDto") &&
							v.getMessage().equals("PostDto mustn't be null."));
				});
    }
	
	@Test
    public void shouldThrowConstraintViolationExceptionWhenDtoNotValid_createNewPost() {

        PostDto postDtoArgNullTitle = buildPostDto(11L, null, "some text", null);
        Long userIdArg = 1L;

		assertThatThrownBy(() -> postService.createNewPost(userIdArg, postDtoArgNullTitle))
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
    public void shouldThrowConstraintViolationExceptionWhenUserIdIsNotPositive_createNewPost() {

        PostDto postDtoArg = buildPostDto(11L, "some title", "some text", null);
        Long userIdArg = -1L;

		assertThatThrownBy(() -> postService.createNewPost(userIdArg, postDtoArg))
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
    public void shouldThrowUserNotFoundException_createNewPost() {

        PostDto postDtoArg = buildPostDto(11L, "test title", "some text", null);
        Long userIdArg = 1L;

        when(userRepository.findById(userIdArg)).thenReturn(Optional.empty());

        Exception exception = assertThrows(UserNotFoundException.class,
                () -> postService.createNewPost(userIdArg, postDtoArg));

        assertEquals("User not found for Id: 1", exception.getMessage());
    }

    @Test
    public void shouldReturnUpdatedPost() {
        Long postIdArg = 1L;
        PostDto postDtoArg = buildPostDto(null, "changed title", "changed text", null);

        User user = buildUser(2L, "test name", "test mail");
        Post postFromDB = buildPost(postIdArg, "other title", "other text");
        postFromDB.setUser(user);

        Post updatedPost = buildPost(postIdArg, "changed title", "changed text");
        updatedPost.setUser(user);

        Post updatedAndSavedPost = buildPost(postIdArg, "changed title", "changed text");
        updatedPost.setUser(user);

        PostDto postDtoFromMapper = buildPostDto(postIdArg, "changed title", "changed text", 2L);

        PostDto expected = buildPostDto(postIdArg, "changed title", "changed text", 2L);

        when(postRepository.findById(postIdArg)).thenReturn(Optional.of(postFromDB));
        when(postRepository.save(updatedPost)).thenReturn(updatedAndSavedPost);
        when(postMapper.toDto(updatedAndSavedPost)).thenReturn(postDtoFromMapper);

        PostDto result = postService.updateExistingPost(postIdArg, postDtoArg);

        assertEquals(expected, result);
    }

    @Test
    public void shouldThrowConstraintViolationException_NullArgs_updateExistingPost() {

		assertThatThrownBy(() -> postService.updateExistingPost(null, null))
				.isInstanceOf(ConstraintViolationException.class)
                .satisfies(exception -> {
					var violations = ((ConstraintViolationException) exception).getConstraintViolations();
					assertThat(violations).hasSize(2);
					assertThat(violations)
						.anyMatch(v -> 
							v.getPropertyPath().toString().contains("postId") &&
							v.getMessage().equals("PostId mustn't be null."))
						.anyMatch(v -> 
							v.getPropertyPath().toString().contains("postDto") &&
							v.getMessage().equals("PostDto mustn't be null."));
				});
    }
	
	@Test
    public void shouldThrowConstraintViolationException_DtoNotValid_updateExistingPost() {

        PostDto postDtoArgNullTitle = buildPostDto(null, null, "some text", 2L);
        Long postIdArg = 1L;

		assertThatThrownBy(() -> postService.updateExistingPost(postIdArg, postDtoArgNullTitle))
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
    public void shouldThrowConstraintViolationException_PostIdNotPositive_updateExistingPost() {

        PostDto postDtoArg = buildPostDto(null, "some title", "some text", 2L);
        Long postIdArg = -1L;

		assertThatThrownBy(() -> postService.updateExistingPost(postIdArg, postDtoArg))
				.isInstanceOf(ConstraintViolationException.class)
                .satisfies(exception -> {
					var violations = ((ConstraintViolationException) exception).getConstraintViolations();
					assertThat(violations).hasSize(1);
					assertThat(violations)
						.anyMatch(v -> 
							v.getPropertyPath().toString().contains("postId") &&
							v.getMessage().equals("PostId must be positive number."));
				});
    }

    @Test
    public void shouldThrowPostNotFoundException_updateExistingPost() {

        PostDto postDtoArg = buildPostDto(null, "test title", "some text", null);
        Long postIdArg = 1L;

        when(userRepository.findById(postIdArg)).thenReturn(Optional.empty());

        Exception exception = assertThrows(PostNotFoundException.class,
                () -> postService.updateExistingPost(postIdArg, postDtoArg));

        assertEquals("Post wasn't found for Id: 1", exception.getMessage());
    }

    @Test
    public void shouldDeletePost() {

        postService.deletePostById(1L);
        verify(postRepository).deleteById(1L);
    }

    @Test
    public void shouldThrowConstraintViolationException_NullArg_deletePostById() {

		assertThatThrownBy(() -> postService.deletePostById(null))
				.isInstanceOf(ConstraintViolationException.class)
                .satisfies(exception -> {
					var violations = ((ConstraintViolationException) exception).getConstraintViolations();
					assertThat(violations).hasSize(1);
					assertThat(violations)
						.anyMatch(v -> 
							v.getPropertyPath().toString().contains("postId") &&
							v.getMessage().equals("PostId mustn't be null."));
				});
    }
	
	@Test
    public void shouldThrowConstraintViolationException_postIdNotPositive_deletePostById() {

		assertThatThrownBy(() -> postService.deletePostById(-1L))
				.isInstanceOf(ConstraintViolationException.class)
                .satisfies(exception -> {
					var violations = ((ConstraintViolationException) exception).getConstraintViolations();
					assertThat(violations).hasSize(1);
					assertThat(violations)
						.anyMatch(v -> 
							v.getPropertyPath().toString().contains("postId") &&
							v.getMessage().equals("PostId must be positive number."));
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

    private User buildUser(Long userId, String username, String email) {
        User user = new User();
        user.setId(userId);
        user.setUserName(username);
        user.setEmail(email);
        user.setCreatedAt(LocalDateTime.now());

        return user;
    }

    private PostDto buildPostDto(Long id, String title, String text, Long userId) {
        return new PostDto(id, title, text, LocalDateTime.now(), userId);
    }
}
