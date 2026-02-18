package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * チャットメッセージエンティティ
 * 
 * このクラスはチャットメッセージを表現し、以下の責務を持ちます:
 * - メッセージの識別（ID）
 * - メッセージの状態管理（ロール、内容、ユーザー、作成日時）
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "chat_messages", indexes = {
    @Index(name = "idx_chat_messages_user_id", columnList = "user_id"),
    @Index(name = "idx_chat_messages_created_at", columnList = "created_at")
})
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //メッセージのロール
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    //メッセージの内容
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    //メッセージの作成日時
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    //ユーザーエンティティの外部キー
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * コンストラクタ
     * 
     * @param role メッセージのロール（"user"、"assistant"、"system"）
     * @param content メッセージの内容
     * @param user ユーザーエンティティ
     * @throws NullPointerException ロール、メッセージ内容、ユーザーがnullの場合
     * @throws IllegalArgumentException メッセージ内容が空文字列の場合
     */
    public ChatMessage(Role role, String content, User user) {
        validate(role, content, user);
        this.role = role;
        this.content = content;
        this.user = user;
        this.createdAt = LocalDateTime.now(); // 作成日時を自動設定
    }

    
    private static void validate(Role role, String content, User user) {
        Objects.requireNonNull(role, "ロールはnullであってはなりません。");
        Objects.requireNonNull(content, "メッセージ内容はnullであってはなりません。");
        if (content.trim().isEmpty()) {
            throw new IllegalArgumentException("メッセージ内容は空文字列であってはなりません。");
        }
        Objects.requireNonNull(user, "ユーザーはnullであってはなりません。");
    }
    
    /**
     * ChatMessageのロール
     * 
     * - "user": ユーザーが送信したメッセージ
     * - "assistant": AIが返答したメッセージ
     * - "system": システムメッセージ（将来の拡張用）
     */
    public enum Role {
        USER,
        ASSISTANT,
        SYSTEM;
    }
}



