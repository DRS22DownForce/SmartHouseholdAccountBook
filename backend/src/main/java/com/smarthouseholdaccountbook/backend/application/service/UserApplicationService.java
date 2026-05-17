package com.smarthouseholdaccountbook.backend.application.service;

import com.smarthouseholdaccountbook.backend.auth.provider.CurrentAuthProvider;
import com.smarthouseholdaccountbook.backend.entity.User;
import com.smarthouseholdaccountbook.backend.exception.UserNotFoundException;
import com.smarthouseholdaccountbook.backend.repository.UserRepository;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ユーザーに関するアプリケーションサービス。
 * ユーザーの取得と、認証済みユーザーのDB登録（UserRegistrationFilterから利用）を担当する。
 */
@Service
public class UserApplicationService {
    /** 「該当 sub のユーザーは DB に存在する」ことを記録する軽量キャッシュ名 */
    private static final String ENSURED_CACHE = "userEnsured";

    private static final Logger logger = LoggerFactory.getLogger(UserApplicationService.class);
    private final UserRepository userRepository;
    private final CurrentAuthProvider currentAuthProvider;
    private final CacheManager cacheManager;

    /**
     * 同一 {@code cognitoSub} に対する登録処理を JVM 内で直列化するためのロック。
     * ロック対象は「ユーザー登録という1度きりの初期化処理」のみに限定し、
     * 使い終わったら {@link ConcurrentMap#remove(Object, Object)} で必ず破棄して
     * マップが肥大化しないようにする（= 攻撃的なサインインを浴びてもメモリリークしない）。
     */
    private final ConcurrentMap<String, Object> ensureLocks = new ConcurrentHashMap<>();

    public UserApplicationService(
            UserRepository userRepository,
            CurrentAuthProvider currentAuthProvider,
            CacheManager cacheManager) {
        this.userRepository = userRepository;
        this.currentAuthProvider = currentAuthProvider;
        this.cacheManager = cacheManager;
    }

    /**
     * 現在の認証ユーザーがDBに存在することを保証する。
     * 存在しなければ新規作成する。UserRegistrationFilter から呼ばれる。
     * <p>
     * パフォーマンスとログノイズの観点から次の2段構造で守っている：
     * <ol>
     *   <li>キャッシュ {@value #ENSURED_CACHE} に「確認済み」が載っていれば即 return（DB ヒットなし）</li>
     *   <li>{@code sub} 単位のロックで並行登録を直列化（初回ログイン時の同時リクエストでも INSERT は1回だけ）</li>
     * </ol>
     */
    public void ensureUserExists() {
        String sub = currentAuthProvider.getCurrentSub();
        // 1段目: キャッシュに「確認済み」が載っていれば DB アクセスなしで抜ける
        if (isEnsured(sub)) {
            return;
        }
        // 2段目: sub 単位のロックで直列化。別ユーザーは互いにブロックしない
        Object lock = ensureLocks.computeIfAbsent(sub, k -> new Object());
        try {
            synchronized (lock) {
                // ロック取得後にもう一度確認（ダブルチェック）。
                if (isEnsured(sub)) {
                    return;
                }
                ensureUserExistsInternal(sub);
                markEnsured(sub);
            }
        } finally {
            ensureLocks.remove(sub, lock);
        }
    }

    /**
     * DB 上の存在確認と、未登録なら INSERT を行う。並行呼び出しはロック済みである前提。
     */
    private void ensureUserExistsInternal(String sub) {
        if (userRepository.findByCognitoSub(sub).isPresent()) {
            return;
        }
        logger.info("ユーザが見つからないため新規作成します");
        String email = currentAuthProvider.getCurrentEmail();
        userRepository.save(new User(sub, email));
    }

    private boolean isEnsured(String sub) {
        Cache cache = cacheManager.getCache(ENSURED_CACHE);
        return cache != null && cache.get(sub) != null;
    }

    private void markEnsured(String sub) {
        Cache cache = cacheManager.getCache(ENSURED_CACHE);
        if (cache != null) {
            cache.put(sub, Boolean.TRUE);
        }
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
