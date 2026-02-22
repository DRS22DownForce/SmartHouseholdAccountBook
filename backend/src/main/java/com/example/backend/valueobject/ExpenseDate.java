package com.example.backend.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

import java.time.LocalDate;
import java.util.Objects;

/**
 * 支出日付を表現する値オブジェクト
 * 
 * このクラスは支出の日付を表現し、以下の責務を持ちます:
 * - 日付のバリデーション（未来日付でないこと）
 * - 日付に関するビジネスロジック
 */
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA用のデフォルトコンストラクタ
@ToString
@Getter
@EqualsAndHashCode
public class ExpenseDate{

    @Column(name = "date", nullable = false)
    private LocalDate date;

    /**
     * コンストラクタ
     * 
     * @param date 支出日付（nullまたは未来日付であってはならない）
     * @throws IllegalArgumentException 日付が未来日付の場合
     * @throws NullPointerException 日付がnullの場合
     */
    public ExpenseDate(LocalDate date) {
        validate(date);
        this.date = date;
    }

    private static void validate(LocalDate value) {
        Objects.requireNonNull(value, "日付はnullであってはなりません。");
        LocalDate today = LocalDate.now();
        if (value.isAfter(today)) {
            throw new IllegalArgumentException("日付は今日以前でなければなりません。");
        }
    }

    /**
     * 日付を比較する（より後か）
     * 
     * @param other 比較する日付
     * @return この日付がotherより後の場合true
     * @throws NullPointerException 比較する日付がnullの場合
     */
    public boolean isAfter(ExpenseDate other) {
        Objects.requireNonNull(other, "比較する日付はnullであってはなりません。");
        return this.date.isAfter(other.date);
    }

    /**
     * 日付を比較する（より前か）
     * 
     * @param other 比較する日付
     * @return この日付がotherより前の場合true
     * @throws NullPointerException 比較する日付がnullの場合
     */
    public boolean isBefore(ExpenseDate other) {
        Objects.requireNonNull(other, "比較する日付はnullであってはなりません。");
        return this.date.isBefore(other.date);
    }

    /**
     * 同じ月かどうかを判定
     * 
     * @param other 比較する日付
     * @return 同じ月の場合true
     * @throws NullPointerException 比較する日付がnullの場合
     */
    public boolean isSameMonth(ExpenseDate other) {
        Objects.requireNonNull(other, "比較する日付はnullであってはなりません。");
        return this.date.getYear() == other.date.getYear() &&
               this.date.getMonthValue() == other.date.getMonthValue();
    }
}

