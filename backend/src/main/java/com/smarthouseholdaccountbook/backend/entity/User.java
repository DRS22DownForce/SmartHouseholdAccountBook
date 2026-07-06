package com.smarthouseholdaccountbook.backend.entity;

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

    // CognitoのユーザーID（個人を特定する最小限の識別子。PII は保存しない）
    @Column(nullable = false, unique = true)
    private String cognitoSub;

    /**
     * ユーザーを作成する
     * @param cognitoSub CognitoのユーザーID（JWT の sub クレーム）
     * @throws NullPointerException cognitoSubがnullの場合
     * @throws IllegalArgumentException cognitoSubが空文字列の場合
     */
    public User(String cognitoSub) {
        validate(cognitoSub);
        this.cognitoSub = cognitoSub;
    }

    private void validate(String cognitoSub) {
        Objects.requireNonNull(cognitoSub, "cognitoSubはnullであってはなりません。");
        if (cognitoSub.trim().isEmpty()) {
            throw new IllegalArgumentException("cognitoSubは空文字列であってはなりません。");
        }
    }
}
