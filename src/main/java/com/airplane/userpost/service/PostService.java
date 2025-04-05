package com.airplane.userpost.service;

import com.airplane.userpost.dto.PostDTO;
import com.airplane.userpost.exception.PostNotFoundException;
import com.airplane.userpost.exception.UserNotFoundException;
import com.airplane.userpost.mapper.PostMapper;
import com.airplane.userpost.model.Post;
import com.airplane.userpost.model.User;
import com.airplane.userpost.repository.PostRepository;
import com.airplane.userpost.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.StreamSupport;

@Slf4j
@Service
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
    public PostDTO getPostById(Long postId) {

        if(postId == null) {
            throw new IllegalArgumentException("Getting Post by Id failed: null postId received");
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("Post not found with Id: " + postId));

        log.info("Post with Id '{}' received from DB", post.getId());

        return postMapper.toDTO(post);
    }

    @Transactional
    public PostDTO createNewPost(Long userId, PostDTO postDTO) {

        if(userId == null || postDTO == null || postDTO.title() == null) {
            throw new IllegalArgumentException("New Post creation failed: bad argument received.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found on DB with Id: " + userId));

        Post post = postMapper.toPost(postDTO);
        post.setId(null);
        post.setUser(user);

        Post savedPost = postRepository.save(post);

        log.info("New post with Id '{}' created.", savedPost.getId());

        return postMapper.toDTO(savedPost);
    }

    @Transactional
    public PostDTO updateExistingPost(Long postId, PostDTO postDTO) {

        if(postId == null || postDTO == null || postDTO.title() == null) {
            throw new IllegalArgumentException("Post update failed: bad argument received.");
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("Post wasn't found in DB for Id: " + postId));

        //no need to update user, id and createdAt fields
        post.setTitle(postDTO.title());
        post.setText(postDTO.text());

        Post updatedPost = postRepository.save(post);
        log.info("Post with Id '{}' updated successfully.", updatedPost.getId());
        return postMapper.toDTO(updatedPost);
    }

    @Transactional
    public void deletePostById(Long postId) {

        if(postId == null) {
            throw new IllegalArgumentException("Post deleteById failed: null postId");
        }
        postRepository.deleteById(postId);

        log.info("Post with Id '{}' deleted successfully.", postId);
    }
}
