package com.example.backend.entity;

import com.example.backend.entity.converter.StringListJsonConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * 月次AIレポートエンティティ
 *
 * ユーザーごとの月次AIレポートをDBに永続化します。
 * 同じ月のレポートを再リクエストした場合はキャッシュを返し、OpenAI APIを再呼び出しません。
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "monthly_reports",
        indexes = { @Index(name = "idx_monthly_reports_user_id_month", columnList = "user_id, month", unique = true) })
public class MonthlyReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 7)
    private String month;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String summary;

    @Convert(converter = StringListJsonConverter.class) //JSON形式に変換して保存
    @Column(name = "suggestions_json", nullable = false, columnDefinition = "TEXT")
    private List<String> suggestions;

    @Column(nullable = false)
    private Instant generatedAt;

    public MonthlyReport(User user, String month, String summary, List<String> suggestions) {
        this.user = Objects.requireNonNull(user, "ユーザーはnullであってはなりません。");
        this.month = Objects.requireNonNull(month, "対象月はnullであってはなりません。");
        this.summary = Objects.requireNonNull(summary, "総評はnullであってはなりません。");
        this.suggestions = Objects.requireNonNull(suggestions, "改善提案はnullであってはなりません。");
        this.generatedAt = Instant.now();
    }

    /**
     * レポート内容を更新する（再生成時に使用）
     */
    public void update(String summary, List<String> suggestions) {
        this.summary = Objects.requireNonNull(summary, "総評はnullであってはなりません。");
        this.suggestions = Objects.requireNonNull(suggestions, "改善提案はnullであってはなりません。");
        this.generatedAt = Instant.now();
    }
}
