package com.example.vida.repository;

import com.example.vida.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    User findUserByEmailAndPassword(String email, String password);

    User findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Page<User> findAll(Specification<User> sepecification, Pageable pageable);
}