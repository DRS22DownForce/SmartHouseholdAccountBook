package com.example.backend.repository;

import com.example.backend.entity.User;
import com.example.backend.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UserRepositoryのテストクラス
 * 
 * ユーザーリポジトリのクエリメソッドをテストします。
 * @DataJpaTestアノテーションにより、JPAレイヤーのみをテストします（実際のデータベースを使用）。
 */
@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    // @Autowired: Springの依存性注入により、リポジトリの実装が自動的に注入されます
    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        // テスト前にデータベースをクリア（各テストが独立して実行されるように）
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Cognitoのsubを指定してユーザーを取得できる")
    void findByCognitoSub_正常に取得() {
        // テストデータの準備: ユーザーを作成して保存
        String cognitoSub = "cognitoSub123";
        String email = "test@example.com";
        User user = new User(cognitoSub, email);
        user = userRepository.save(user);

        // テスト実行: Cognitoのsubを指定してユーザーを取得
        Optional<User> foundUser = userRepository.findByCognitoSub(cognitoSub);

        // 検証: ユーザーが正しく取得できることを確認
        assertTrue(foundUser.isPresent());
        assertEquals(cognitoSub, foundUser.get().getCognitoSub());
        assertEquals(email, foundUser.get().getEmail());
    }

    @Test
    @DisplayName("存在しないCognitoのsubを指定した場合は空のOptionalが返される")
    void findByCognitoSub_存在しない場合() {
        // テスト実行: 存在しないCognitoのsubで取得
        Optional<User> foundUser = userRepository.findByCognitoSub("nonExistentSub");

        // 検証: 空のOptionalが返されることを確認
        assertFalse(foundUser.isPresent());
    }

    @Test
    @DisplayName("複数のユーザーが存在する場合でも正しいユーザーを取得できる")
    void findByCognitoSub_複数のユーザーが存在する場合() {
        // テストデータの準備: 複数のユーザーを作成
        User user1 = new User("cognitoSub1", "user1@example.com");
        User user2 = new User("cognitoSub2", "user2@example.com");
        User user3 = new User("cognitoSub3", "user3@example.com");
        userRepository.save(user1);
        userRepository.save(user2);
        userRepository.save(user3);

        // テスト実行: 特定のCognitoのsubで取得
        Optional<User> foundUser = userRepository.findByCognitoSub("cognitoSub2");

        // 検証: 正しいユーザーが取得できることを確認
        assertTrue(foundUser.isPresent());
        assertEquals("cognitoSub2", foundUser.get().getCognitoSub());
        assertEquals("user2@example.com", foundUser.get().getEmail());
    }

    @Test
    @DisplayName("ユーザーを保存できる")
    void save_正常に保存() {
        // テストデータの準備
        String cognitoSub = "cognitoSub123";
        String email = "test@example.com";
        User user = new User(cognitoSub, email);

        // テスト実行: ユーザーを保存
        User savedUser = userRepository.save(user);

        // 検証: 保存されたユーザーが正しいことを確認
        assertNotNull(savedUser.getId()); // IDが自動生成されていることを確認
        assertEquals(cognitoSub, savedUser.getCognitoSub());
        assertEquals(email, savedUser.getEmail());
    }

    @Test
    @DisplayName("保存したユーザーを取得できる")
    void saveAndFind_正常に保存と取得() {
        // テストデータの準備
        String cognitoSub = "cognitoSub123";
        String email = "test@example.com";
        User user = new User(cognitoSub, email);

        // テスト実行: ユーザーを保存
        User savedUser = userRepository.save(user);

        // テスト実行: 保存したユーザーを取得
        Optional<User> foundUser = userRepository.findById(savedUser.getId());

        // 検証: 保存したユーザーが正しく取得できることを確認
        assertTrue(foundUser.isPresent());
        assertEquals(cognitoSub, foundUser.get().getCognitoSub());
        assertEquals(email, foundUser.get().getEmail());
    }

    @Test
    @DisplayName("ユーザーを削除できる")
    void delete_正常に削除() {
        // テストデータの準備: ユーザーを作成して保存
        String cognitoSub = "cognitoSub123";
        String email = "test@example.com";
        User user = new User(cognitoSub, email);
        User savedUser = userRepository.save(user);

        // テスト実行: ユーザーを削除
        userRepository.delete(savedUser);

        // 検証: 削除されたユーザーが取得できないことを確認
        Optional<User> foundUser = userRepository.findById(savedUser.getId());
        assertFalse(foundUser.isPresent());
    }
}

