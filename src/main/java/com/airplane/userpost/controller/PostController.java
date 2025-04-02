package com.airplane.userpost.controller;

import com.airplane.userpost.dto.PostDTO;
import com.airplane.userpost.service.PostService;
import com.airplane.userpost.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/posts", produces = "application/json")
public class PostController {
    private final PostService postService;

    @Autowired
    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    public ResponseEntity<List<PostDTO>> allPosts() {
        log.info("AllPosts request received.");

        return ResponseEntity.ok(postService.getAllPosts());
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<PostDTO> postById(@PathVariable Long id) {
        log.info("PostById request received");

        return ResponseEntity.ok(postService.getPostById(id));
    }

    @PostMapping(path = "/{userId}")
    public ResponseEntity<PostDTO> newPost(@PathVariable Long userId, @RequestBody PostDTO postDTO) {

    }
}
