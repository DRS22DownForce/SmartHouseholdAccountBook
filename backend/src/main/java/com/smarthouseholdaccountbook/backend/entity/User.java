package com.example.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.Column;
import lombok.AccessLevel;
import java.util.Objects;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class User {
    // アプリ内でユーザーを管理するためのID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // CognitoのユーザーID
    @Column(nullable = false, unique = true)
    private String cognitoSub;

    @Column(nullable = false)
    private String email;

    /**
     * ユーザーを作成する
     * @param cognitoSub CognitoのユーザーID
     * @param email メールアドレス
     * @throws NullPointerException cognitoSubまたはemailがnullの場合
     * @throws IllegalArgumentException cognitoSubが空文字列の場合、emailが空文字列の場合、emailに@が含まれていない場合
     */
    public User(String cognitoSub, String email) {
        validate(cognitoSub, email);
        this.cognitoSub = cognitoSub;
        this.email = email;
    }

    private void validate(String cognitoSub, String email) {
        Objects.requireNonNull(cognitoSub, "cognitoSubはnullであってはなりません。");
        if (cognitoSub.trim().isEmpty()) {
            throw new IllegalArgumentException("cognitoSubは空文字列であってはなりません。");
        }
        Objects.requireNonNull(email, "emailはnullであってはなりません。");
        if (email.trim().isEmpty()) {
            throw new IllegalArgumentException("emailは空文字列であってはなりません。");
        }
        if (!email.contains("@")) {
            throw new IllegalArgumentException("メールアドレスの形式が正しくありません。");
        }
    }
}