package com.airplane.userpost.controller;

import com.airplane.userpost.dto.UserDTO;
import com.airplane.userpost.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping(path = "/users", produces = "application/json")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<UserDTO>> allUsers() {
        log.info("AllUsers request received.");

        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<UserDTO> userById(@PathVariable Long id) {
        log.info("User request with id '{}' received.", id);

        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PostMapping
    public ResponseEntity<UserDTO> newUser(@Valid @RequestBody UserDTO userDTO) {
        log.info("NewUser request received. Username: {}, email: {}",
                userDTO.getUserName(),
                userDTO.getEmail());

        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createNewUser(userDTO));
    }

    @PutMapping(path = "/{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @Valid @RequestBody UserDTO userDTO) {
        log.info("Update user request received for id '{}'", id);

        return ResponseEntity.status(HttpStatus.OK).body(userService.updateExistingUser(id, userDTO));
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.info("Delete user request received for id '{}'", id);

        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
