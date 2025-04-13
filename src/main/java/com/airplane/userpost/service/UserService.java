package com.airplane.userpost.service;

import com.airplane.userpost.dto.PostDto;
import com.airplane.userpost.dto.UserDto;
import com.airplane.userpost.exception.UserNotFoundException;
import com.airplane.userpost.mapper.UserMapper;
import com.airplane.userpost.model.Post;
import com.airplane.userpost.model.User;
import com.airplane.userpost.repository.PostRepository;
import com.airplane.userpost.repository.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Set;
import java.util.stream.StreamSupport;

@Slf4j
@Service
@Validated
public class UserService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final UserMapper userMapper;

    @Autowired
    public UserService(UserRepository userRepository, PostRepository postRepository, UserMapper userMapper) {

        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.userMapper = userMapper;
    }

    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {

        Iterable<User> allUsers = userRepository.findAll();
        log.info("All users retrieved from DB.");
        return StreamSupport.stream(allUsers.spliterator(), false)
                .map(userMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserDto getUserById(@NotNull(message = "UserId mustn't be null.")
							@Positive(message = "UserId must be positive number.") Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with Id: " + userId));

        log.info("User with Id '{}' received.", userId);
		
        return userMapper.toDto(user);
    }

    @Transactional
    public UserDto createNewUser(@NotNull(message = "UserDto mustn't be null.") @Valid UserDto userDto) {
		
		//Post creation is not allowed here
		userDto.getPosts().clear();
		
        User newUser = userMapper.toUser(userDto);
        newUser.setId(null);

        User savedUser = userRepository.save(newUser);
		
        log.info("New user with Id '{}' created.", savedUser.getId());

        return userMapper.toDto(savedUser);
    }

    //do not updates CreatedAt field
    @Transactional
    public UserDto updateExistingUser(@NotNull(message = "UserId mustn't be null.")
									@Positive(message = "UserId must be positive number.") Long userId,
                                      @NotNull(message = "UserDto mustn't be null.") @Valid UserDto userDto) {

        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with Id: " + userId));

        existingUser.setUserName(userDto.getUserName());
        log.info("User with Id {}. Username updated.", existingUser.getId());

        existingUser.setEmail(userDto.getEmail());
        log.info("User with Id '{}'. Email updated.", existingUser.getId());

        Set<Post> posts = existingUser.getPosts();
        posts.clear();
        for(PostDto postDto: userDto.getPosts()) {
			Post post = null;
			if(postDto.id() == null) {
				post = new Post();
			}
			else {
				post = postRepository.findById(postDto.id()).orElse(new Post());
			}
			post.setTitle(postDto.title());
			post.setText(postDto.text());
			post.setUser(existingUser);
			posts.add(post);
        }

        //update user
        User savedUser = userRepository.save(existingUser);
        log.info("User with Id '{}' updated successfully.", existingUser.getId());
        return userMapper.toDto(savedUser);
    }

    @Transactional
    public void deleteUser(@NotNull(message = "UserId mustn't be null.")
						@Positive(message = "UserId must be positive number.") Long userId) {
        
        userRepository.deleteById(userId);
		
		log.info("User with Id '{}' deleted.", userId);
    }
}
