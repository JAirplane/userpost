package com.airplane.userpost.repository;

import com.airplane.userpost.model.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {
    Boolean existsByUserName(String userName);
    Boolean existsByEmail(String email);
}
