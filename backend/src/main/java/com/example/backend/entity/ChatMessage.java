package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
    private Long id; // 主キー（識別子）

    /**
     * メッセージのロール
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    /**
     * メッセージの内容
     * TEXT型を使用することで、長いメッセージも保存可能
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content; // メッセージの内容

    /**
     * メッセージの作成日時
     * 自動的に現在時刻が設定されます
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt; // 作成日時

    /**
     * ユーザーエンティティの外部キー
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * コンストラクタ
     * 
     * @param role メッセージのロール（"user"、"assistant"、"system"）
     * @param content メッセージの内容
     * @param user ユーザーエンティティ
     */
    public ChatMessage(Role role, String content, User user) {
        validate(role, content, user);
        this.role = role;
        this.content = content;
        this.user = user;
        this.createdAt = LocalDateTime.now(); // 作成日時を自動設定
    }

    /**
     * バリデーションロジック
     * 
     * エンティティレベルでの整合性チェックを行います。
     */
    private static void validate(Role role, String content, User user) {
        if (role == null) {
            throw new IllegalArgumentException("ロールは必須です。");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("メッセージ内容は必須です。");
        }
        if (user == null) {
            throw new IllegalArgumentException("ユーザーは必須です。");
        }
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



