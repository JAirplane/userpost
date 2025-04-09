package com.airplane.userpost.service;

import com.airplane.userpost.dto.PostDTO;
import com.airplane.userpost.exception.PostNotFoundException;
import com.airplane.userpost.exception.UserNotFoundException;
import com.airplane.userpost.mapper.PostMapper;
import com.airplane.userpost.model.Post;
import com.airplane.userpost.model.User;
import com.airplane.userpost.repository.PostRepository;
import com.airplane.userpost.repository.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.stream.StreamSupport;

@Slf4j
@Service
@Validated
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostMapper postMapper;

    @Autowired
    public PostService(PostRepository postRepository, UserRepository userRepository, PostMapper postMapper) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.postMapper = postMapper;
    }

    @Transactional(readOnly = true)
    public List<PostDTO> getAllPosts() {

        Iterable<Post> posts = postRepository.findAll();
        log.info("All posts retrieved from DB.");

        return StreamSupport.stream(posts.spliterator(), false)
                .map(postMapper::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public PostDTO getPostById(@NotNull(message = "PostId mustn't be null.") Long postId) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("Post not found for Id: " + postId));

        log.info("Post with Id '{}' found.", post.getId());

        return postMapper.toDTO(post);
    }

    @Transactional
    public PostDTO createNewPost(@NotNull(message = "UserId mustn't be null.") Long userId,
			@NotNull(message = "PostDTO mustn't be null.") @Valid PostDTO postDTO) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found for Id: " + userId));

        Post post = postMapper.toPost(postDTO);
        post.setId(null);
        post.setUser(user);

        Post savedPost = postRepository.save(post);

        log.info("New post with Id '{}' created.", savedPost.getId());

        return postMapper.toDTO(savedPost);
    }

    @Transactional
    public PostDTO updateExistingPost(@NotNull(message = "PostId mustn't be null.") Long postId,
			@NotNull(message = "PostDTO mustn't be null.") @Valid PostDTO postDTO) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("Post wasn't found for Id: " + postId));

        //no need to update user, id and createdAt fields
        post.setTitle(postDTO.title());
        post.setText(postDTO.text());

        Post updatedPost = postRepository.save(post);
        log.info("Post with Id '{}' updated.", updatedPost.getId());
        return postMapper.toDTO(updatedPost);
    }

    @Transactional
    public void deletePostById(@NotNull(message = "PostId mustn't be null.") Long postId) {

        postRepository.deleteById(postId);

        log.info("Post with Id '{}' deleted.", postId);
    }
}
