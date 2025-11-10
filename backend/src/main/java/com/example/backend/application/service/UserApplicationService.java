package com.example.backend.application.service;

import com.example.backend.auth.provider.CurrentAuthProvider;
import com.example.backend.domain.repository.UserRepository;
import com.example.backend.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ユーザーに関するアプリケーションサービス
 * このクラスはユーザーの取得、作成というユースケースを実装します。
 */
@Service
@Transactional // トランザクション管理
public class UserApplicationService {
    private static final Logger logger = LoggerFactory.getLogger(UserApplicationService.class);
    private final UserRepository userRepository;
    private final CurrentAuthProvider currentAuthProvider;

    /**
     * コンストラクタ
     * 
     * @param userRepository ユーザーリポジトリ
     * @param currentAuthProvider 現在の認証プロバイダー
     */
    public UserApplicationService(UserRepository userRepository, CurrentAuthProvider currentAuthProvider) {
        this.userRepository = userRepository;
        this.currentAuthProvider = currentAuthProvider;
    }

    /**
     * ユーザーが存在しない場合は作成するユースケース
     * 
     * 現在の認証情報からユーザーを取得し、存在しない場合は新規作成します。
     * 
     * @return ユーザーエンティティ（認証情報が取得できない場合はnull）
     */
    public User createUserIfNotExists() {
        // 1. 認証情報を取得
        String sub = currentAuthProvider.getCurrentSub();
        String email = currentAuthProvider.getCurrentEmail();
        
        if (sub != null) {
            // 2. 既存のユーザーを検索
            return userRepository.findByCognitoSub(sub)
                    .orElseGet(() -> {
                        // 3. 存在しない場合は新規作成
                        User user = new User(sub, email);
                        return userRepository.save(user);
                    });
        }
        
        // 4. 認証情報が取得できない場合は警告をログに記録
        logger.warn("subがnullです。認証情報が正しく取得できていません。");
        return null;
    }

    /**
     * 現在のユーザーを取得するユースケース
     * 
     * 現在の認証情報からユーザーを取得します。
     * 
     * @return ユーザーエンティティ
     * @throws RuntimeException ユーザーが見つからない場合
     */
    @Transactional(readOnly = true) // 読み取り専用トランザクション
    public User getUser() {
        // 1. 認証情報を取得
        String sub = currentAuthProvider.getCurrentSub();
        
        // 2. ユーザーを検索
        return userRepository.findByCognitoSub(sub)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}

