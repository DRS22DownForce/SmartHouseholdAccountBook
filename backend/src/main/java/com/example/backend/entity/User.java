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

    public User(String cognitoSub, String email) {
        validate(cognitoSub, email);
        this.cognitoSub = cognitoSub;
        this.email = email;
    }

    private static void validate(String cognitoSub, String email) {
        if (cognitoSub == null || cognitoSub.trim().isEmpty()) {
            throw new IllegalArgumentException("cognitoSubは必須です。");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("emailは必須です。");
        }
        if (!email.contains("@")) {
            throw new IllegalArgumentException("メールアドレスの形式が正しくありません。");
        }
    }
}