package com.airplane.userpost.repository;

import com.airplane.userpost.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {

    @Override
    @EntityGraph(attributePaths = "posts")
    @NonNull
    List<User> findAll();
}
