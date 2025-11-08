package com.example.backend.service;

import com.example.backend.auth.provider.CurrentAuthProvider;
import com.example.backend.entity.User;
import com.example.backend.repository.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final CurrentAuthProvider currentAuthProvider;

    public UserService(UserRepository userRepository, CurrentAuthProvider currentAuthProvider) {
        this.userRepository = userRepository;
        this.currentAuthProvider = currentAuthProvider;
    }

    public User createUserIfNotExists() {
        String sub = currentAuthProvider.getCurrentSub();
        String email = currentAuthProvider.getCurrentEmail();
        if (sub != null) {
            return userRepository.findByCognitoSub(sub)
                    .orElseGet(() -> {
                        User user = new User(sub, email);
                        return userRepository.save(user);
                    });
        }
        logger.warn("subがnullです。認証情報が正しく取得できていません。");
        return null;
    }

    public User getUser() {
        String sub = currentAuthProvider.getCurrentSub();
        return userRepository.findByCognitoSub(sub)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
