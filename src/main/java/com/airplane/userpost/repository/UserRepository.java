package com.airplane.userpost.repository;

import com.airplane.userpost.model.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {
    Boolean existByUserName(String userName);
    Boolean existByEmail(String email);
}
