package com.airplane.userpost.mapper;

import com.airplane.userpost.dto.PostDTO;
import com.airplane.userpost.exception.MapperException;
import com.airplane.userpost.model.Post;
import org.springframework.stereotype.Component;


@Component
public class PostMapper {

    public PostDTO toDTO(Post post) {
        if(post == null) throw new MapperException("Mapper received null Post.");
        if(post.getUser() == null) throw new MapperException("Mapper received Post with null User.");

        return new PostDTO(post.getId(), post.getTitle(),
                post.getText(), post.getCreatedAt(), post.getUser().getId());
    }

    //No User setting here
    public Post toPost(PostDTO postDTO) {
        if(postDTO == null) throw new MapperException("Mapper received null PostDTO.");

        Post post = new Post();
        post.setId(postDTO.id());
        post.setTitle(postDTO.title());
        post.setText(postDTO.text());

        return post;
    }
}
