package com.smarthouseholdaccountbook.backend.repository;

import com.smarthouseholdaccountbook.backend.entity.User;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UserRepositoryのテストクラス
 */
@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User createUser(String cognitoSub) {
        return userRepository.save(new User(cognitoSub));
    }

    @Nested
    @DisplayName("findByCognitoSub - Cognitoのsubでユーザー取得")
    class FindByCognitoSub {

        @Test
        @DisplayName("Cognitoのsubを指定してユーザーを取得できる")
        void returnsUserWhenSubExists() {
            // given
            String cognitoSub = "cognitoSub123";
            createUser(cognitoSub);

            // when
            Optional<User> foundUser = userRepository.findByCognitoSub(cognitoSub);

            // then
            assertThat(foundUser).isPresent();
            assertThat(foundUser.get().getCognitoSub()).isEqualTo(cognitoSub);
        }

        @Test
        @DisplayName("存在しないCognitoのsubを指定した場合は空のOptionalが返される")
        void returnsEmptyWhenSubNotExists() {
            // when
            Optional<User> foundUser = userRepository.findByCognitoSub("nonExistentSub");

            // then
            assertThat(foundUser).isEmpty();
        }

        @Test
        @DisplayName("複数のユーザーが存在する場合でも正しいユーザーを取得できる")
        void returnsCorrectUserWhenMultipleUsersExist() {
            // given
            createUser("cognitoSub1");
            createUser("cognitoSub2");
            createUser("cognitoSub3");

            // when
            Optional<User> foundUser = userRepository.findByCognitoSub("cognitoSub2");

            // then
            assertThat(foundUser).isPresent();
            assertThat(foundUser.get().getCognitoSub()).isEqualTo("cognitoSub2");
        }
    }

    @Nested
    @DisplayName("save - ユーザー保存")
    class Save {

        @Test
        @DisplayName("ユーザーを保存できる")
        void savesUserWithGeneratedId() {
            // given
            String cognitoSub = "cognitoSub123";
            User user = new User(cognitoSub);

            // when
            User savedUser = userRepository.save(user);

            // then
            assertThat(savedUser.getId()).isNotNull();
            assertThat(savedUser.getCognitoSub()).isEqualTo(cognitoSub);
        }

        @Test
        @DisplayName("保存したユーザーを取得できる")
        void savedUserCanBeFoundById() {
            // given
            String cognitoSub = "cognitoSub123";
            User savedUser = createUser(cognitoSub);

            // when
            Optional<User> foundUser = userRepository.findById(savedUser.getId());

            // then
            assertThat(foundUser).isPresent();
            assertThat(foundUser.get().getCognitoSub()).isEqualTo(cognitoSub);
        }
    }

    @Nested
    @DisplayName("delete - ユーザー削除")
    class Delete {

        @Test
        @DisplayName("ユーザーを削除できる")
        void deletesUser() {
            // given
            User savedUser = createUser("cognitoSub123");

            // when
            userRepository.delete(savedUser);

            // then
            Optional<User> foundUser = userRepository.findById(savedUser.getId());
            assertThat(foundUser).isEmpty();
        }
    }
}
