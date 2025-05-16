package io.documentprocessing.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import io.documentprocessing.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
    Optional<User> findById(Long id);
}

