package com.example.backend.repository;

import com.example.backend.entity.User;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // 基本的なCRUD操作はJpaRepositoryから継承されます
    Optional<User> findByCognitoSub(String cognitoSub);
}
