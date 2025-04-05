package com.airplane.userpost.controller;

import com.airplane.userpost.dto.PostDTO;
import com.airplane.userpost.service.PostService;
import com.airplane.userpost.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

    @GetMapping(path = "/{postId}")
    public ResponseEntity<PostDTO> postById(@PathVariable Long postId) {
        log.info("Get Post request for Id {} received", postId);

        return ResponseEntity.ok(postService.getPostById(postId));
    }

    @PostMapping(path = "/{userId}")
    public ResponseEntity<PostDTO> newPost(@PathVariable Long userId, @RequestBody PostDTO postDTO) {
        log.info("Create Post request for User Id {} received", userId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(postService.createNewPost(userId, postDTO));
    }

    @PutMapping(path = "/{postId}")
    public ResponseEntity<PostDTO> updatePost(@PathVariable Long postId, @RequestBody PostDTO postDTO) {
        log.info("Update Post request for Id {} received", postId);

        return ResponseEntity.ok()
                .body(postService.updateExistingPost(postId, postDTO));
    }

    @DeleteMapping(path = "/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable Long postId) {
        log.info("Delete Post request for Id {} received", postId);

        postService.deletePostById(postId);
        return ResponseEntity.noContent().build();
    }
}
