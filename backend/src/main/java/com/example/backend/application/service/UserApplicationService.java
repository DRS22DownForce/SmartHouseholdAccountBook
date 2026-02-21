package com.example.backend.application.service;

import com.example.backend.auth.provider.CurrentAuthProvider;
import com.example.backend.domain.repository.UserRepository;
import com.example.backend.entity.User;
import com.example.backend.exception.UserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;

/**
 * ユーザーに関するアプリケーションサービス。
 * ユーザーの取得と、認証済みユーザーのDB登録（UserRegistrationFilterから利用）を担当する。
 */
@Service
@Transactional
public class UserApplicationService {
    private static final Logger logger = LoggerFactory.getLogger(UserApplicationService.class);
    private final UserRepository userRepository;
    private final CurrentAuthProvider currentAuthProvider;

    public UserApplicationService(UserRepository userRepository, CurrentAuthProvider currentAuthProvider) {
        this.userRepository = userRepository;
        this.currentAuthProvider = currentAuthProvider;
    }

    /**
     * 現在の認証ユーザーがDBに存在することを保証する。
     * 存在しなければ新規作成する。UserRegistrationFilter から呼ばれ、常に別トランザクションで実行する。
     */
    public void ensureUserExists() {
        String sub = currentAuthProvider.getCurrentSub();
        if (userRepository.findByCognitoSub(sub).isPresent()) {
            return;
        }
        logger.info("ユーザが見つからないため新規作成します。cognitoSub: {}", sub);
        String email = currentAuthProvider.getCurrentEmail();
        User user = new User(sub, email);
        userRepository.save(user);
    }

    /**
     * 現在の認証情報に紐づくユーザーを取得する。
     * UserRegistrationFilter により事前にDBに登録されている前提。
     *
     * @return ユーザーエンティティ
     * @throws UserNotFoundException ユーザーが存在しない場合（通常はフィルター未実行時）
     */
    @Cacheable(value = "users", key = "@currentAuthProvider.getCurrentSub()")
    @Transactional(readOnly = true)
    public User getUser() {
        String sub = currentAuthProvider.getCurrentSub();
        return userRepository.findByCognitoSub(sub)
                .orElseThrow(UserNotFoundException::new);
    }
}
