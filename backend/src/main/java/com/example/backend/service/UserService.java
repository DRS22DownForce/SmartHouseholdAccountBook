package com.example.backend.service;

import com.example.backend.auth.CurrentAuthProvider;
import com.example.backend.entity.User;
import com.example.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final CurrentAuthProvider currentAuthProvider;

    public UserService(UserRepository userRepository, CurrentAuthProvider currentAuthProvider) {
        this.userRepository = userRepository;
        this.currentAuthProvider = currentAuthProvider;
    }

    public User createUserIfNotExists() {
        String sub = currentAuthProvider.getCurrentSub();
        String email = currentAuthProvider.getCurrentEmail();
        return userRepository.findByCognitoSub(sub)
                .orElseGet(() -> {
                    User user = new User(sub, email);
                    return userRepository.save(user);
                });
    }

    public User getUser() {
        String sub = currentAuthProvider.getCurrentSub();
        return userRepository.findByCognitoSub(sub)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
