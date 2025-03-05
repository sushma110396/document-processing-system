package io.documentprocessing.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import io.documentprocessing.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
}

