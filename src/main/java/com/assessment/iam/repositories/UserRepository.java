package com.assessment.iam.repositories;

import org.springframework.data.repository.CrudRepository;

import com.assessment.iam.entities.User;

import java.util.List;
import java.util.Optional;


public interface UserRepository extends CrudRepository<User, String> {

    long countByEmail(String email);
    Optional<User> findByEmail(String email);
    List<User> findAllByUserNameEndsWith(String tenantId);
}