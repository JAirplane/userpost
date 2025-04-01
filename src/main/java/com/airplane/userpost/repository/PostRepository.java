package com.airplane.userpost.repository;

import com.airplane.userpost.model.Post;
import org.springframework.data.repository.CrudRepository;

public interface PostRepository extends CrudRepository<Post, Long> {
}
