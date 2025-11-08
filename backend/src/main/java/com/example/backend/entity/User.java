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
import lombok.EqualsAndHashCode;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(exclude = "id") // idはDBの自動生成値なので、equalsとhashCodeでは除外する
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
        this.cognitoSub = cognitoSub;
        this.email = email;
    }
}