package com.airplane.userpost.service;

import com.airplane.userpost.dto.UserDTO;
import com.airplane.userpost.mapper.UserMapper;
import com.airplane.userpost.model.User;
import com.airplane.userpost.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Autowired
    public UserService(UserRepository userRepository, UserMapper userMapper) {

        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    public List<UserDTO> getAllUsers() {

        Iterable<User> allUsers = userRepository.findAll();
        log.info("All users retrieved from db.");
        return StreamSupport.stream(allUsers.spliterator(), false)
                .map(userMapper::toDTO)
                .toList();
    }

    public Optional<UserDTO> getUserById(Long userId) {

        Optional<User> user = userRepository.findById(userId);
        log.info("User with id {} {}.",
                userId,
                user.map(u -> "exists").orElse("doesn't exist"));
        return user.map(userMapper::toDTO).or(Optional::empty);
    }

    @Transactional
    public Optional<UserDTO> createNewUser(UserDTO userDTO) {
        if(userDTO == null) {
            log.warn("CreateNewUser received null DTO");
            return Optional.empty();
        }

        Optional<User> userByName = userRepository.findByUserName(userDTO.userName());
        if(userByName.isPresent()) return Optional.empty();

        Optional<User> userByEmail = userRepository.findByEmail(userDTO.email());
        if(userByEmail.isPresent()) return Optional.empty();

        User newUser = userMapper.toUser(userDTO);
        User savedUser = userRepository.save(newUser);
        log.info("New user with id {} created.", savedUser.getId());

        return Optional.of(userMapper.toDTO(savedUser));
    }

    //do not updates CreatedAt field
    @Transactional
    public Optional<UserDTO> updateExistingUser(Long userId, UserDTO userDTO) {

        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) return Optional.empty();
        User existingUser = user.get();

        if(userDTO == null) {
            return Optional.of(userMapper.toDTO(existingUser));
        }

        //checks that new username unique
        if (userDTO.userName() != null && !existingUser.getUserName().equals(userDTO.userName())) {
            Optional<User> userByName = userRepository.findByUserName(userDTO.userName());
            if (userByName.isPresent()) return Optional.empty();
            existingUser.setUserName(userDTO.userName());
            log.info("User id: {}. Username updated.", existingUser.getId());
        }

        //checks that new email unique
        if (userDTO.email() != null && !existingUser.getEmail().equals(userDTO.email())) {
            Optional<User> userByEmail = userRepository.findByEmail(userDTO.email());
            if (userByEmail.isPresent()) return Optional.empty();
            existingUser.setEmail(userDTO.email());
            log.info("User id: {}. Email updated.", existingUser.getId());
        }

        //update user
        User savedUser = userRepository.save(existingUser);
        log.info("User id: {}. Updated successfully.", existingUser.getId());
        return Optional.of(userMapper.toDTO(savedUser));
    }

    public void deleteUser(Long userId) {
        log.info("User id: {}. Deleted.", userId);
        userRepository.deleteById(userId);
    }
}
