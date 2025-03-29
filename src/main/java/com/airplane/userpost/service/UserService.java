package com.airplane.userpost.service;

import com.airplane.userpost.dto.UserDTO;
import com.airplane.userpost.mapper.UserMapper;
import com.airplane.userpost.model.User;
import com.airplane.userpost.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

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
        return StreamSupport.stream(allUsers.spliterator(), false)
                .map(userMapper::toDTO)
                .toList();
    }

    public Optional<UserDTO> getUserById(Long userId) {

        Optional<User> user = userRepository.findById(userId);
        return user.map(userMapper::toDTO).or(Optional::empty);
    }

    @Transactional
    public Optional<UserDTO> createNewUser(UserDTO userDTO) {

        Optional<User> userByName = userRepository.findByUserName(userDTO.userName());
        if(userByName.isPresent()) return Optional.empty();

        Optional<User> userByEmail = userRepository.findByEmail(userDTO.email());
        if(userByEmail.isPresent()) return Optional.empty();

        User newUser = new User();
        newUser.setUserName(userDTO.userName());
        newUser.setEmail(userDTO.email());
        newUser.setCreatedAt(LocalDateTime.now());
        User savedUser = userRepository.save(newUser);

        return Optional.of(new UserDTO(savedUser.getId(),
                savedUser.getUserName(),
                savedUser.getEmail(),
                savedUser.getCreatedAt())
        );
    }

    //do not updates CreatedAt field
    @Transactional
    public Optional<UserDTO> updateExistingUser(Long userId, UserDTO userDTO) {

        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) return Optional.empty();
        User existingUser = user.get();

        //checks that new username unique
        if (!existingUser.getUserName().equals(userDTO.userName())) {
            Optional<User> userByName = userRepository.findByUserName(userDTO.userName());
            if (userByName.isPresent()) return Optional.empty();
            existingUser.setUserName(userDTO.userName());
        }

        //checks that new email unique
        if (!existingUser.getEmail().equals(userDTO.email())) {
            Optional<User> userByEmail = userRepository.findByEmail(userDTO.email());
            if (userByEmail.isPresent()) return Optional.empty();
            existingUser.setEmail(userDTO.email());
        }

        //update user
        User savedUser = userRepository.save(existingUser);
        return Optional.of(new UserDTO(savedUser.getId(),
                savedUser.getUserName(),
                savedUser.getEmail(),
                savedUser.getCreatedAt())
        );
    }

    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }
}
