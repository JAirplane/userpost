package com.airplane.userpost.service;

import com.airplane.userpost.dto.UserDTO;
import com.airplane.userpost.exception.UserNotFoundException;
import com.airplane.userpost.mapper.UserMapper;
import com.airplane.userpost.model.User;
import com.airplane.userpost.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
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

    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {

        Iterable<User> allUsers = userRepository.findAll();
        log.info("All users retrieved from db.");
        return StreamSupport.stream(allUsers.spliterator(), false)
                .map(userMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<UserDTO> getUserById(Long userId) {

        Optional<User> user = userRepository.findById(userId);
        if(user.isEmpty()) {
            String error = "User with Id " + userId + " not found";
            throw new UserNotFoundException(error);
        }

        log.info("User with Id {} received.", userId);
        return user.map(userMapper::toDTO);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Optional<UserDTO> createNewUser(UserDTO userDTO) {
        if(userDTO == null) {
            throw new IllegalArgumentException("User creation failed: null DTO received");
        }

        if(userDTO.userName() == null || userDTO.email() == null) {
            throw new IllegalArgumentException("User creation failed: bad DTO field");
        }

        Boolean usernameExist = userRepository.existByUserName(userDTO.userName());
        if(usernameExist) {
            throw new DataIntegrityViolationException("User creation failed: username already exists");
        }

        Boolean emailExist = userRepository.existByEmail(userDTO.email());
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

        Optional<User> user = userRepository.findById(userId);
        if(user.isEmpty()) {
            String error = "User with Id " + userId + " not found";
            throw new UserNotFoundException(error);
        }

        User existingUser = user.get();

        //checks that new username unique
        if (userDTO.userName() != null && !existingUser.getUserName().equals(userDTO.userName())) {
            Boolean usernameExist = userRepository.existByUserName(userDTO.userName());
            if(usernameExist) {
                throw new DataIntegrityViolationException("Username update failed: username already exists");
            }

            existingUser.setUserName(userDTO.userName());
            log.info("User id: {}. Username updated.", existingUser.getId());
        }

        //checks that new email unique
        if (userDTO.email() != null && !existingUser.getEmail().equals(userDTO.email())) {
            Boolean emailExist = userRepository.existByEmail(userDTO.email());
            if(emailExist) {
                throw new DataIntegrityViolationException("Email update failed: email already exists");
            }

            existingUser.setEmail(userDTO.email());
            log.info("User id: {}. Email updated.", existingUser.getId());
        }

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
