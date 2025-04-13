package com.airplane.userpost.mapper;

import com.airplane.userpost.dto.PostDto;
import com.airplane.userpost.exception.MapperException;
import com.airplane.userpost.model.Post;
import org.springframework.stereotype.Component;


@Component
public class PostMapper {

    public PostDto toDto(Post post) {
        if(post == null) throw new MapperException("Mapper received null Post.");
        if(post.getUser() == null) throw new MapperException("Mapper received Post with null User.");

        return new PostDto(post.getId(), post.getTitle(),
                post.getText(), post.getCreatedAt(), post.getUser().getId());
    }

    //No User setting here
    public Post toPost(PostDto postDto) {
        if(postDto == null) throw new MapperException("Mapper received null PostDto.");

        Post post = new Post();
        post.setId(postDto.id());
        post.setTitle(postDto.title());
        post.setText(postDto.text());

        return post;
    }
}
