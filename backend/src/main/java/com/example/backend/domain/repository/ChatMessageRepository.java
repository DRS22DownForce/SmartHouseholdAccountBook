package com.example.backend.domain.repository;

import com.example.backend.entity.ChatMessage;
import com.example.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * チャットメッセージエンティティのリポジトリインターフェース
 */
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    /**
     * ユーザーを指定してメッセージを取得
     * 
     * 作成日時の降順（新しい順）でソートして返します。
     * 
     * @param user ユーザーエンティティ
     * @return 該当ユーザーのメッセージリスト（新しい順）
     */
    @Query("SELECT m FROM ChatMessage m WHERE m.user = :user ORDER BY m.createdAt DESC")
    List<ChatMessage> findByUserOrderByCreatedAtDesc(@Param("user") User user);
}



