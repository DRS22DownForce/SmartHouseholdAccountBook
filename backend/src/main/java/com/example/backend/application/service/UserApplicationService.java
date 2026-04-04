package com.example.backend.application.service;

import com.example.backend.auth.provider.CurrentAuthProvider;
import com.example.backend.entity.User;
import com.example.backend.exception.UserNotFoundException;
import com.example.backend.repository.UserRepository;

import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;

/**
 * ユーザーに関するアプリケーションサービス。
 * ユーザーの取得と、認証済みユーザーのDB登録（UserRegistrationFilterから利用）を担当する。
 */
@Service
public class UserApplicationService {
    /** Flyway V1 の UNIQUE キー名（重複判定に使用） */
    private static final String UK_USERS_COGNITO_SUB = "uk_users_cognito_sub";

    private static final Logger logger = LoggerFactory.getLogger(UserApplicationService.class);
    private final UserRepository userRepository;
    private final CurrentAuthProvider currentAuthProvider;
    private final UserRegistrationTxService userRegistrationTxService;

    public UserApplicationService(
            UserRepository userRepository,
            CurrentAuthProvider currentAuthProvider,
            UserRegistrationTxService userRegistrationTxService) {
        this.userRepository = userRepository;
        this.currentAuthProvider = currentAuthProvider;
        this.userRegistrationTxService = userRegistrationTxService;
    }

    /**
     * 現在の認証ユーザーがDBに存在することを保証する。
     * 存在しなければ新規作成する。UserRegistrationFilter から呼ばれる。
     * <p>
     * ログイン直後に複数 API が同時に来ると「存在チェック → INSERT」の隙間で競合し、
     * {@code cognito_sub} の一意制約違反が起きうる。INSERT は {@link UserRegistrationTxService} の
     * {@code REQUIRES_NEW} で行い、その制約違反だけ冪等扱いする。
     */
    public void ensureUserExists() {
        String sub = currentAuthProvider.getCurrentSub();
        if (userRepository.findByCognitoSub(sub).isPresent()) {
            return;
        }
        logger.info("ユーザが見つからないため新規作成します。cognitoSub: {}", sub);
        String email = currentAuthProvider.getCurrentEmail();
        try {
            userRegistrationTxService.insertNewUser(sub, email);
        } catch (DataIntegrityViolationException ex) {
            if (!isDuplicateCognitoSubConstraint(ex)) {
                throw ex;
            }
            // 別スレッドが先に INSERT 済み。想定外なら行が無いはずなので確認する。
            if (userRepository.findByCognitoSub(sub).isEmpty()) {
                logger.error("cognito_sub の重複エラーだが行が見つかりません。cognitoSub: {}", sub);
                throw ex;
            }
            logger.debug("並行登録により一意制約に達したためスキップします。cognitoSub: {}", sub);
        }
    }

    /**
     * MySQL の重複エントリ（1062 / SQLState 23000）かつ {@code uk_users_cognito_sub} 由来か判定する。
     * 他 UNIQUE 制約の誤飲み込みを避けるため、インデックス名をメッセージで確認する。
     */
    private static boolean isDuplicateCognitoSubConstraint(DataIntegrityViolationException ex) {
        Throwable cause = ex.getMostSpecificCause();
        if (cause instanceof SQLException sqlEx) {
            if ("23000".equals(sqlEx.getSQLState()) && sqlEx.getErrorCode() == 1062) {
                String msg = sqlEx.getMessage();
                return msg != null && msg.contains(UK_USERS_COGNITO_SUB);
            }
        }
        String msg = ex.getMessage();
        return msg != null && msg.contains(UK_USERS_COGNITO_SUB);
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
