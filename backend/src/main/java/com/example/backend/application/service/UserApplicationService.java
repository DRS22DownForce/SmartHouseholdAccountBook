package com.example.backend.application.service;

import com.example.backend.auth.provider.CurrentAuthProvider;
import com.example.backend.domain.repository.UserRepository;
import com.example.backend.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;

/**
 * ユーザーに関するアプリケーションサービス
 * このクラスはユーザーの取得、作成というユースケースを実装します。
 */
@Service
@Transactional
public class UserApplicationService {
    private static final Logger logger = LoggerFactory.getLogger(UserApplicationService.class);
    private final UserRepository userRepository;
    private final CurrentAuthProvider currentAuthProvider;

    /**
     * コンストラクタ
     * 
     * @param userRepository      ユーザーリポジトリ
     * @param currentAuthProvider 現在の認証プロバイダー
     */
    public UserApplicationService(UserRepository userRepository, CurrentAuthProvider currentAuthProvider) {
        this.userRepository = userRepository;
        this.currentAuthProvider = currentAuthProvider;
    }

    /**
     * 現在のユーザーを取得するユースケース
     * 
     * 現在の認証情報からユーザーを取得します。
     * 存在しない場合は新規作成します。
     * キャッシュを利用してユーザーを取得します。
     * 
     * @return ユーザーエンティティ
     */
    @Cacheable(value = "users", key = "@currentAuthProvider.getCurrentSub()")
    @Transactional(readOnly = true)
    public User getUser() {
        String sub = currentAuthProvider.getCurrentSub();
        return userRepository.findByCognitoSub(sub)
                .orElseGet(() -> {
                    logger.info("ユーザが見つからないため新規作成します。cognitoSub: {}", sub);
                    String email = currentAuthProvider.getCurrentEmail();
                    User user = new User(sub, email);
                    return userRepository.save(user);
                });
    }
}
