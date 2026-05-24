package com.smarthouseholdaccountbook.backend.valueobject;

import com.smarthouseholdaccountbook.backend.application.service.csv.model.CsvParsedExpense;
import com.smarthouseholdaccountbook.backend.entity.Expense;

import java.time.LocalDate;
import java.util.Objects;

/**
 * CSVインポート時の重複判定用キー
 *
 * 日付・金額・説明の組み合わせで「同じ取引」かどうかを判定します。
 * record の equals/hashCode により Set での重複チェックに利用します。
 */
public record ExpenseDuplicateKey(
        LocalDate date,
        int amount,
        String description) {

    /**
     * CSV解析結果から重複判定キーを生成する
     */
    public static ExpenseDuplicateKey from(CsvParsedExpense parsed) {
        Objects.requireNonNull(parsed, "parsedはnullであってはなりません");
        return new ExpenseDuplicateKey(parsed.date(), parsed.amount(), parsed.description());
    }

    /**
     * 既存の支出エンティティから重複判定キーを生成する
     */
    public static ExpenseDuplicateKey from(Expense expense) {
        Objects.requireNonNull(expense, "expenseはnullであってはなりません");
        return new ExpenseDuplicateKey(
                expense.getDate().getDate(),
                expense.getAmount().getAmount(),
                expense.getDescription());
    }
}
