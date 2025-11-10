package com.example.backend.domain.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;

/**
 * 支出日付を表現する値オブジェクト
 * 
 * このクラスは支出の日付を表現し、以下の責務を持ちます:
 * - 日付のバリデーション（未来日付でないこと）
 * - 日付に関するビジネスロジック
 */
@Embeddable
@Getter
@ToString
@EqualsAndHashCode
public class ExpenseDate{

    @Column(name = "date", nullable = false)
    private final LocalDate value;

    /**
     * JPA用のデフォルトコンストラクタ
     */
    protected ExpenseDate() {
        this.value = null;
    }

    /**
     * コンストラクタ
     * 
     * @param value 支出日付（nullまたは未来日付であってはならない）
     * @throws IllegalArgumentException 日付がnullまたは未来日付の場合
     */
    public ExpenseDate(LocalDate value) {
        validate(value);
        this.value = value;
    }

    /**
     * バリデーション: 日付が有効かチェック
     * 
     * @param value 検証する日付
     * @throws IllegalArgumentException 日付がnullまたは未来日付の場合
     */
    private static void validate(LocalDate value) {
        if (value == null) {
            throw new IllegalArgumentException("日付はnullであってはなりません。");
        }
        LocalDate today = LocalDate.now();
        if (value.isAfter(today)) {
            throw new IllegalArgumentException("日付は今日以前でなければなりません。");
        }
    }

    /**
     * LocalDate値として取得（DTO変換などで使用）
     * 
     * @return 日付のLocalDate値
     */
    public LocalDate toLocalDate() {
        return this.value;
    }

    /**
     * 年を取得
     * 
     * @return 年
     */
    public int getYear() {
        return this.value.getYear();
    }

    /**
     * 月を取得
     * 
     * @return 月（1-12）
     */
    public int getMonthValue() {
        return this.value.getMonthValue();
    }

    /**
     * 日を取得
     * 
     * @return 日（1-31）
     */
    public int getDayOfMonth() {
        return this.value.getDayOfMonth();
    }

    /**
     * 日付を比較する（より後か）
     * 
     * @param other 比較する日付
     * @return この日付がotherより後の場合true
     */
    public boolean isAfter(ExpenseDate other) {
        if (other == null) {
            throw new IllegalArgumentException("比較する日付はnullであってはなりません。");
        }
        return this.value.isAfter(other.value);
    }

    /**
     * 日付を比較する（より前か）
     * 
     * @param other 比較する日付
     * @return この日付がotherより前の場合true
     */
    public boolean isBefore(ExpenseDate other) {
        if (other == null) {
            throw new IllegalArgumentException("比較する日付はnullであってはなりません。");
        }
        return this.value.isBefore(other.value);
    }

    /**
     * 同じ月かどうかを判定
     * 
     * @param other 比較する日付
     * @return 同じ月の場合true
     */
    public boolean isSameMonth(ExpenseDate other) {
        if (other == null) {
            return false;
        }
        return this.value.getYear() == other.value.getYear() &&
               this.value.getMonthValue() == other.value.getMonthValue();
    }
}

