package com.airplane.userpost.service;

import com.airplane.userpost.dto.PostDTO;
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

    @Test
    public void shouldThrowConstraintViolationExceptionWhenPostIdIsNull() {

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
    public void shouldThrowPostNotFoundExceptionWhenPostNotFoundInDB() {

        when(postRepository.findById(100L)).thenReturn(Optional.empty());
        Exception exception = assertThrows(PostNotFoundException.class,
                () -> postService.getPostById(100L));

        assertEquals("Post not found for Id: 100", exception.getMessage());
    }

    @Test
    public void shouldReturnCreatedPostDTO() {
        Long userIdArg = 1L;
        PostDTO postDTOArg = testPostDTO(11L, "test title", "some text", null);

        User user = testUser(userIdArg, "test name", "test mail");

        Post postFromMapper = testPost(null, "test title", "some text");
        postFromMapper.setUser(user);

        Post savedPost = testPost(2L, "test title", "some text");
        savedPost.setUser(user);

        PostDTO postDTOFromMapper = testPostDTO(2L, "test title", "some text", user.getId());

        PostDTO expected = testPostDTO(2L, "test title", "some text", userIdArg);

        when(userRepository.findById(userIdArg)).thenReturn(Optional.of(user));
        when(postMapper.toPost(postDTOArg)).thenReturn(postFromMapper);
        when(postRepository.save(postFromMapper)).thenReturn(savedPost);
        when(postMapper.toDTO(savedPost)).thenReturn(postDTOFromMapper);

        PostDTO result = postService.createNewPost(userIdArg, postDTOArg);

        assertEquals(expected, result);
    }

    @Test
    public void shouldThrowConstraintViolationExceptionWhenNullArguments() {

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
							v.getPropertyPath().toString().contains("postDTO") &&
							v.getMessage().equals("PostDTO mustn't be null."));
				});
    }
	
	@Test
    public void shouldThrowConstraintViolationExceptionWhenDtoNotValid() {

        PostDTO postDTOArgNullTitle = testPostDTO(11L, null, "some text", null);
        Long userIdArg = 1L;

		assertThatThrownBy(() -> postService.createNewPost(userIdArg, postDTOArgNullTitle))
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
    public void shouldThrowUserNotFoundExceptionWhenCreatingNewPost() {

        PostDTO postDTOArg = testPostDTO(11L, "test title", "some text", null);
        Long userIdArg = 1L;

        when(userRepository.findById(userIdArg)).thenReturn(Optional.empty());

        Exception exception = assertThrows(UserNotFoundException.class,
                () -> postService.createNewPost(userIdArg, postDTOArg));

        assertEquals("User not found for Id: 1", exception.getMessage());
    }

    @Test
    public void shouldReturnUpdatedPost() {
        Long postIdArg = 1L;
        PostDTO postDTOArg = testPostDTO(null, "changed title", "changed text", null);

        User user = testUser(2L, "test name", "test mail");
        Post postFromDB = testPost(postIdArg, "other title", "other text");
        postFromDB.setUser(user);

        Post updatedPost = testPost(postIdArg, "changed title", "changed text");
        updatedPost.setUser(user);

        Post updatedAndSavedPost = testPost(postIdArg, "changed title", "changed text");
        updatedPost.setUser(user);

        PostDTO postDTOFromMapper = testPostDTO(postIdArg, "changed title", "changed text", 2L);

        PostDTO expected = testPostDTO(postIdArg, "changed title", "changed text", 2L);

        when(postRepository.findById(postIdArg)).thenReturn(Optional.of(postFromDB));
        when(postRepository.save(updatedPost)).thenReturn(updatedAndSavedPost);
        when(postMapper.toDTO(updatedAndSavedPost)).thenReturn(postDTOFromMapper);

        PostDTO result = postService.updateExistingPost(postIdArg, postDTOArg);

        assertEquals(expected, result);
    }

    @Test
    public void shouldThrowConstraintViolationExceptionWhenUpdateExistingPostWithNullArgs() {

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
							v.getPropertyPath().toString().contains("postDTO") &&
							v.getMessage().equals("PostDTO mustn't be null."));
				});
    }
	
	@Test
    public void shouldThrowConstraintViolationExceptionWhenUpdateExistingPostWithDTONotValid() {

        PostDTO postDTOArgNullTitle = testPostDTO(null, null, "some text", 2L);
        Long postIdArg = 1L;

		assertThatThrownBy(() -> postService.updateExistingPost(postIdArg, postDTOArgNullTitle))
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
    public void shouldThrowPostNotFoundExceptionWhenUpdatingPost() {

        PostDTO postDTOArg = testPostDTO(null, "test title", "some text", null);
        Long postIdArg = 1L;

        when(userRepository.findById(postIdArg)).thenReturn(Optional.empty());

        Exception exception = assertThrows(PostNotFoundException.class,
                () -> postService.updateExistingPost(postIdArg, postDTOArg));

        assertEquals("Post wasn't found for Id: 1", exception.getMessage());
    }

    @Test
    public void shouldDeletePost() {

        postService.deletePostById(1L);
        verify(postRepository).deleteById(1L);
    }

    @Test
    public void shouldThrowConstraintViolationExceptionWhenDeletePostWithNullIdArg() {

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
