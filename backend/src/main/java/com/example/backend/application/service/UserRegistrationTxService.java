package com.example.backend.application.service;

import com.example.backend.entity.User;
import com.example.backend.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * ユーザー登録用の短いトランザクション境界。
 * <p>
 * {@code REQUIRES_NEW} で INSERT だけを囲み、並行リクエストによる一意制約違反時に
 * 呼び出し元のトランザクション／永続化コンテキストを汚さないようにする。
 * 同一クラス内の {@code this} 呼び出しでは {@code @Transactional} が効かないため、Bean を分離している。
 */
@Service
public class UserRegistrationTxService {

    private final UserRepository userRepository;

    public UserRegistrationTxService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 新規ユーザーを永続化する。重複 {@code cognito_sub} の場合は DB が例外を返す。
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void insertNewUser(String cognitoSub, String email) {
        userRepository.save(new User(cognitoSub, email));
    }
}
