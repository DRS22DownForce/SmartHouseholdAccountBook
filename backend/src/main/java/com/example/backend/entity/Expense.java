package com.example.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity // このアノテーションは、このクラスがJPAのエンティティであることを示します。
@Table(name = "household_accounts") // テーブル名の指定
@Data // getter, setter, equals, hashCode, toStringを自動生成
@NoArgsConstructor // JPAが要求する引数なしコンストラクタを生成
@AllArgsConstructor // Builderパターンで必要な全引数コンストラクタを生成
@Builder // ビルダーパターンを実装（内部的に全引数コンストラクタも生成）
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 主キー

    private String description; // 支出の説明
    private Integer amount; // 金額
    private LocalDate date; // 日付
    private String category; // カテゴリー
    private LocalDateTime createdAt; // 作成日時
}