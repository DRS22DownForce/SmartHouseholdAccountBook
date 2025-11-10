package com.example.backend.domain.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * 支出金額を表現する値オブジェクト
 * 
 * このクラスは支出の金額を表現し、以下の責務を持ちます:
 * - 金額のバリデーション（1以上であること）
 * - 金額に関するビジネスロジック（加算など）
 */
@Embeddable
@Getter
@ToString
@EqualsAndHashCode
public class ExpenseAmount{

    @Column(name = "amount", nullable = false)
    private final Integer value;

    /**
     * JPA用のデフォルトコンストラクタ
     */
    protected ExpenseAmount() {
        this.value = null;
    }

    /**
     * コンストラクタ
     * 
     * @param value 金額（1以上でなければならない）
     * @throws IllegalArgumentException 金額がnullまたは0以下の場合
     */
    public ExpenseAmount(Integer value) {
        validate(value);
        this.value = value;
    }

    /**
     * バリデーション: 金額が有効かチェック
     * 
     * @param value 検証する金額
     * @throws IllegalArgumentException 金額がnullまたは0以下の場合
     */
    private static void validate(Integer value) {
        if (value == null) {
            throw new IllegalArgumentException("金額はnullであってはなりません。");
        }
        if (value <= 0) {
            throw new IllegalArgumentException("金額は1以上でなければなりません。");
        }
    }

    /**
     * 金額を加算する
     * 
     * 値オブジェクトは不変なので、新しいインスタンスを返します。
     * 
     * @param other 加算する金額
     * @return 加算結果の新しいExpenseAmountインスタンス
     */
    public ExpenseAmount add(ExpenseAmount other) {
        if (other == null) {
            throw new IllegalArgumentException("加算する金額はnullであってはなりません。");
        }
        return new ExpenseAmount(this.value + other.value);
    }

    /**
     * 金額を減算する
     * 
     * @param other 減算する金額
     * @return 減算結果の新しいExpenseAmountインスタンス
     * @throws IllegalArgumentException 減算結果が0以下になる場合
     */
    public ExpenseAmount subtract(ExpenseAmount other) {
        if (other == null) {
            throw new IllegalArgumentException("減算する金額はnullであってはなりません。");
        }
        int result = this.value - other.value;
        if (result <= 0) {
            throw new IllegalArgumentException("減算結果は1以上でなければなりません。");
        }
        return new ExpenseAmount(result);
    }

    /**
     * 金額を比較する（より大きいか）
     * 
     * @param other 比較する金額
     * @return この金額がotherより大きい場合true
     */
    public boolean isGreaterThan(ExpenseAmount other) {
        if (other == null) {
            throw new IllegalArgumentException("比較する金額はnullであってはなりません。");
        }
        return this.value > other.value;
    }

    /**
     * 金額を比較する（より小さいか）
     * 
     * @param other 比較する金額
     * @return この金額がotherより小さい場合true
     */
    public boolean isLessThan(ExpenseAmount other) {
        if (other == null) {
            throw new IllegalArgumentException("比較する金額はnullであってはなりません。");
        }
        return this.value < other.value;
    }

    /**
     * Integer値として取得（DTO変換などで使用）
     * 
     * @return 金額のInteger値
     */
    public Integer toInteger() {
        return this.value;
    }
}

