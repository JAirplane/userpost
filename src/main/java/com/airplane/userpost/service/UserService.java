package com.airplane.userpost.service;

import com.airplane.userpost.dto.PostDTO;
import com.airplane.userpost.dto.UserDTO;
import com.airplane.userpost.exception.UserNotFoundException;
import com.airplane.userpost.mapper.UserMapper;
import com.airplane.userpost.model.Post;
import com.airplane.userpost.model.User;
import com.airplane.userpost.repository.PostRepository;
import com.airplane.userpost.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.StreamSupport;

@Slf4j
@Service
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
    public List<UserDTO> getAllUsers() {

        Iterable<User> allUsers = userRepository.findAll();
        log.info("All users retrieved from DB.");
        return StreamSupport.stream(allUsers.spliterator(), false)
                .map(userMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserDTO getUserById(Long userId) {

        if(userId == null) {
            throw new IllegalArgumentException("Getting User by Id failed: null Id received");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with Id: " + userId));

        log.info("User with Id {} received.", userId);
        return userMapper.toDTO(user);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public UserDTO createNewUser(UserDTO userDTO) {

        if(userDTO == null) {
            throw new IllegalArgumentException("User creation failed: null DTO received");
        }

        if(userDTO.getUserName() == null || userDTO.getEmail() == null || userDTO.getId() != null) {
            throw new IllegalArgumentException("User creation failed: bad DTO field");
        }

        User newUser = userMapper.toUser(userDTO);
        User savedUser = userRepository.save(newUser);
        log.info("New user with id {} created.", savedUser.getId());

        return userMapper.toDTO(savedUser);
    }

    //do not updates CreatedAt field
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public UserDTO updateExistingUser(Long userId, UserDTO userDTO) {

        if(userId == null || userDTO == null) {
            throw new IllegalArgumentException("User update failed: received null arg");
        }

        if(userDTO.getUserName() == null || userDTO.getEmail() == null) {
            throw new IllegalArgumentException("User creation failed: bad DTO field");
        }

        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User update failed: user not found with Id: " + userId));

        existingUser.setUserName(userDTO.getUserName());
        log.info("User id: {}. Username updated.", existingUser.getId());

        existingUser.setEmail(userDTO.getEmail());
        log.info("User id: {}. Email updated.", existingUser.getId());

        Set<Post> posts = existingUser.getPosts();
        posts.clear();
        for(PostDTO postDTO: userDTO.getPosts()) {
            if(postDTO.title() != null) {
                Post post = null;
                if(postDTO.id() == null) {
                    post = new Post();
                }
                else {
                    post = postRepository.findById(postDTO.id()).orElse(new Post());
                }
                post.setTitle(postDTO.title());
                post.setText(postDTO.text());
                post.setUser(existingUser);
                posts.add(post);
            }
        }

        //update user
        User savedUser = userRepository.save(existingUser);
        log.info("User id: {}. Updated successfully.", existingUser.getId());
        return userMapper.toDTO(savedUser);
    }

    @Transactional
    public void deleteUser(Long userId) {
        log.info("User id: {}. Deleted.", userId);
        userRepository.deleteById(userId);
    }
}
