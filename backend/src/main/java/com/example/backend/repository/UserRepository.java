package com.example.backend.repository;

import com.example.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * ユーザーエンティティのリポジトリインターフェース
 */
public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * Cognitoのsub（ユーザーID）を指定してユーザーを取得
     * 
     * @param cognitoSub CognitoのユーザーID
     * @return 該当するユーザー（存在しない場合は空）
     */
    Optional<User> findByCognitoSub(String cognitoSub);
}

