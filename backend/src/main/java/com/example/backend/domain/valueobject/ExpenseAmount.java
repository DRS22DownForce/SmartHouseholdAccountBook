package com.example.backend.domain.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import lombok.Getter;
import java.util.Objects;

/**
 * 支出金額を表現する値オブジェクト
 * 
 * このクラスは支出の金額を表現し、以下の責務を持ちます:
 * - 金額のバリデーション（1以上であること）
 * - 金額に関するビジネスロジック（加算など）
 */
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA用のデフォルトコンストラクタ
@ToString
@Getter
@EqualsAndHashCode
public class ExpenseAmount{

    @Column(name = "amount", nullable = false)
    private Integer amount;

    /**
     * コンストラクタ
     * 
     * @param value 金額（1以上でなければならない）
     * @throws IllegalArgumentException 金額がnullまたは0以下の場合
     */
    public ExpenseAmount(Integer value) {
        validate(value);
        this.amount = value;
    }

    /**
     * バリデーション: 金額が有効かチェック
     * 
     * @param value 検証する金額
     * @throws IllegalArgumentException 金額が0以下の場合
     * @throws NullPointerException 金額がnullの場合
     */
    private static void validate(Integer value) {
        Objects.requireNonNull(value, "金額はnullであってはなりません。");
        if (value < 1) {
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
     * @throws NullPointerException 加算する金額がnullの場合
     */
    public ExpenseAmount add(ExpenseAmount other) {
        Objects.requireNonNull(other, "加算する金額はnullであってはなりません。");
        return new ExpenseAmount(this.amount + other.amount);
    }

    /**
     * 金額を比較する（より大きいか）
     * 
     * @param other 比較する金額
     * @return この金額がotherより大きい場合true
     * @throws NullPointerException 比較する金額がnullの場合
     */
    public boolean isGreaterThan(ExpenseAmount other) {
        Objects.requireNonNull(other, "比較する金額はnullであってはなりません。");
        return this.amount > other.amount;
    }

    /**
     * 金額を比較する（より小さいか）
     * 
     * @param other 比較する金額
     * @return この金額がotherより小さい場合true
     * @throws NullPointerException 比較する金額がnullの場合
     */
    public boolean isLessThan(ExpenseAmount other) {
        Objects.requireNonNull(other, "比較する金額はnullであってはなりません。");
        return this.amount < other.amount;
    }
}

