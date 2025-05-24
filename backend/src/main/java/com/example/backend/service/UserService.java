package com.example.backend.service;

import com.example.backend.entity.User;
import com.example.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUserIfNotExists(String cognitoSub, String email) {
        return userRepository.findByCognitoSub(cognitoSub)
            .orElseGet(() -> {
                User user = new User(cognitoSub, email);
                return userRepository.save(user);
            });
    }
}
