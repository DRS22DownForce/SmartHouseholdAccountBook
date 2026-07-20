package com.smarthouseholdaccountbook.backend.application.service;

import com.smarthouseholdaccountbook.backend.application.service.csv.model.CsvParsedExpense;
import com.smarthouseholdaccountbook.backend.entity.Expense;
import com.smarthouseholdaccountbook.backend.entity.User;
import com.smarthouseholdaccountbook.backend.repository.ExpenseRepository;
import com.smarthouseholdaccountbook.backend.valueobject.ExpenseDuplicateKey;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * CSV 支出インポートに伴う DB アクセス専用サービス。
 *
 * OpenAI 呼び出しなどの外部 I/O とは TX を分離し、読み取り・保存それぞれ短い TX のみを張る。
 */
@Service
public class CsvExpensePersistenceService {

    private final ExpenseRepository expenseRepository;

    public CsvExpensePersistenceService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    /**
     * DB に既存の行と重複する CSV 行を除外し、新規行のみを返す。
     * 同一 CSV 内でキーが同じ行は別取引の可能性があるため、除外しない。
     */
    @Transactional(readOnly = true)
    public FilterNewExpensesResult filterNewExpenses(List<CsvParsedExpense> parsedExpenses, User user) {
        LocalDate minDate = parsedExpenses.stream()
                .map(CsvParsedExpense::date)
                .min(Comparator.naturalOrder())
                .orElseThrow();
        LocalDate maxDate = parsedExpenses.stream()
                .map(CsvParsedExpense::date)
                .max(Comparator.naturalOrder())
                .orElseThrow();

        Set<ExpenseDuplicateKey> existingKeys = expenseRepository
                .findByUserAndDateBetween(user, minDate, maxDate)
                .stream()
                .map(ExpenseDuplicateKey::from)
                .collect(Collectors.toCollection(HashSet::new));

        List<CsvParsedExpense> newExpenses = new ArrayList<>();
        int skippedCount = 0;

        for (CsvParsedExpense parsed : parsedExpenses) {
            ExpenseDuplicateKey key = ExpenseDuplicateKey.from(parsed);
            if (existingKeys.contains(key)) {
                skippedCount++;
            } else {
                newExpenses.add(parsed);
            }
        }

        return new FilterNewExpensesResult(newExpenses, skippedCount);
    }

    /**
     * 支出エンティティを一括保存する。
     */
    @Transactional
    public int saveExpenses(List<Expense> expenses) {
        return expenseRepository.saveAll(expenses).size();
    }

    /**
     * 重複除外フィルタの結果。
     *
     * @param newExpenses  保存対象の新規行
     * @param skippedCount スキップした行数（DB 既存との重複）
     */
    public record FilterNewExpensesResult(
            List<CsvParsedExpense> newExpenses,
            int skippedCount) {
    }
}
