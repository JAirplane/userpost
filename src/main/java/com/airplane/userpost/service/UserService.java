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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
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

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Optional<UserDTO> createNewUser(UserDTO userDTO) {

        if(userDTO == null) {
            throw new IllegalArgumentException("User creation failed: null DTO received");
        }

        if(userDTO.getUserName() == null || userDTO.getEmail() == null || userDTO.getId() != null) {
            throw new IllegalArgumentException("User creation failed: bad DTO field");
        }

        Boolean usernameExist = userRepository.existsByUserName(userDTO.getUserName());
        if(usernameExist) {
            throw new DataIntegrityViolationException("User creation failed: username already exists");
        }

        Boolean emailExist = userRepository.existsByEmail(userDTO.getEmail());
        if(emailExist) {
            throw new DataIntegrityViolationException("User creation failed: email already exists");
        }

        User newUser = userMapper.toUser(userDTO);
        User savedUser = userRepository.save(newUser);
        log.info("New user with id {} created.", savedUser.getId());

        return Optional.of(userMapper.toDTO(savedUser));
    }

    //do not updates CreatedAt field
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Optional<UserDTO> updateExistingUser(Long userId, UserDTO userDTO) {

        if(userDTO == null) {
            throw new IllegalArgumentException("User update failed: received null DTO");
        }

        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User update failed: user not found with Id " + userId));

        //checks that new username unique
        if (userDTO.getUserName() != null && !existingUser.getUserName().equals(userDTO.getUserName())) {
            Boolean usernameExist = userRepository.existsByUserName(userDTO.getUserName());
            if(usernameExist) {
                throw new DataIntegrityViolationException("Username update failed: username already exists");
            }

            existingUser.setUserName(userDTO.getUserName());
            log.info("User id: {}. Username updated.", existingUser.getId());
        }

        //checks that new email unique
        if (userDTO.getEmail() != null && !existingUser.getEmail().equals(userDTO.getEmail())) {
            Boolean emailExist = userRepository.existsByEmail(userDTO.getEmail());
            if(emailExist) {
                throw new DataIntegrityViolationException("Email update failed: email already exists");
            }

            existingUser.setEmail(userDTO.getEmail());
            log.info("User id: {}. Email updated.", existingUser.getId());
        }

        Set<Post> posts = new HashSet<>();
        for(PostDTO postDTO: userDTO.getPosts()) {
            if(postDTO.title() != null) {
                Post post = postRepository.findById(postDTO.id()).orElse(new Post());
                post.setTitle(postDTO.title());
                post.setText(postDTO.text());
                post.setUser(existingUser);
                posts.add(post);
            }
        }
        existingUser.setPosts(posts);

        //update user
        User savedUser = userRepository.save(existingUser);
        log.info("User id: {}. Updated successfully.", existingUser.getId());
        return Optional.of(userMapper.toDTO(savedUser));
    }

    @Transactional
    public void deleteUser(Long userId) {
        log.info("User id: {}. Deleted.", userId);
        userRepository.deleteById(userId);
    }
}
