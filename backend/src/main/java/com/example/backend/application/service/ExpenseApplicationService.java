package com.example.backend.application.service;

import com.example.backend.domain.repository.ExpenseRepository;
import com.example.backend.domain.valueobject.MonthlySummary;
import com.example.backend.entity.Expense;
import com.example.backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.backend.exception.ExpenseNotFoundException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 支出に関するアプリケーションサービス
 * このクラスは支出の追加、取得、更新、削除というユースケースを実装します。
 */
@Service
@Transactional
public class ExpenseApplicationService {
    private static final String MONTH_FORMAT = "yyyy-MM";
    private final ExpenseRepository expenseRepository;
    private final UserApplicationService userApplicationService;

    /**
     * コンストラクタ
     *
     * @param expenseRepository      支出リポジトリ
     * @param userApplicationService ユーザーアプリケーションサービス
     */
    public ExpenseApplicationService(
            ExpenseRepository expenseRepository,
            UserApplicationService userApplicationService) {
        this.expenseRepository = expenseRepository;
        this.userApplicationService = userApplicationService;
    }

    /**
     * 月文字列（yyyy-MM）を YearMonth にパースする。
     * 形式が不正な場合は IllegalArgumentException を投げる。
     */
    private static YearMonth parseMonth(String month) {
        try {
            return YearMonth.parse(month, DateTimeFormatter.ofPattern(MONTH_FORMAT));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("月の形式が不正です。yyyy-MM で指定してください: " + month, e);
        }
    }

    /**
     * 全ての支出を取得するユースケース
     *
     * 現在のユーザーの支出エンティティのリストを返します。
     *
     * @return 支出エンティティのリスト
     */
    @Transactional(readOnly = true)
    public List<Expense> getExpenses() {
        User user = userApplicationService.getUser();
        return expenseRepository.findByUser(user);
    }

    /**
     * 新しい支出を追加するユースケース
     *
     * 作成内容（ExpenseUpdate）と現在ユーザーからエンティティを生成し、保存して返します。
     *
     * @param creation 支出の作成内容（説明・金額・日付・カテゴリ）
     * @return 保存後の支出エンティティ
     */
    public Expense addExpense(Expense.ExpenseUpdate creation) {
        User user = userApplicationService.getUser();
        Expense expense = new Expense(
                creation.description(),
                creation.amount(),
                creation.date(),
                creation.category(),
                user);
        return expenseRepository.save(expense);
    }

    /**
     * 支出を削除するユースケース
     * 
     * 指定されたIDの支出を削除します。
     * 
     * @param id 支出ID
     */
    public void deleteExpense(Long id) {
        User user = userApplicationService.getUser();
        Expense existingExpense = expenseRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ExpenseNotFoundException(id));
        expenseRepository.delete(existingExpense);
    }

    /**
     * 支出を更新するユースケース
     *
     * 既存の支出を取得し、更新内容（ExpenseUpdate）を適用して保存し、エンティティを返します。
     *
     * @param id    支出ID
     * @param update 更新内容（説明・金額・日付・カテゴリ）
     * @return 更新後の支出エンティティ
     */
    public Expense updateExpense(Long id, Expense.ExpenseUpdate update) {
        User user = userApplicationService.getUser();
        Expense existingExpense = expenseRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ExpenseNotFoundException(id));
        existingExpense.update(update);
        return expenseRepository.save(existingExpense);
    }

    /**
     * 月別支出を取得するユースケース（ページネーション対応）
     *
     * 指定された月の支出エンティティのページを返します。
     * H2とMySQLの両方で動作するように、日付範囲を使用してクエリします。
     *
     * @param month    月（YYYY-MM形式）
     * @param pageable ページネーション情報
     * @return 支出エンティティのページ
     */
    @Transactional(readOnly = true)
    public Page<Expense> getExpensesByMonth(@NonNull String month, Pageable pageable) {
        YearMonth yearMonth = parseMonth(month);
        User user = userApplicationService.getUser();

        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        return expenseRepository.findByUserAndDateRange(user, startDate, endDate, pageable);
    }

    /**
     * 月別サマリーを取得するユースケース
     * 
     * 指定された月の支出を集計し、MonthlySummary値オブジェクトを作成して返します。
     * 
     * @param month 月（YYYY-MM形式）
     * @return 月別サマリー値オブジェクト
     */
    @Transactional(readOnly = true)
    public MonthlySummary getMonthlySummary(String month) {
        YearMonth yearMonth = parseMonth(month);

        User user = userApplicationService.getUser();

        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<Expense> expenses = expenseRepository.findByUserAndDateBetween(user, startDate, endDate);

        return MonthlySummary.from(expenses);
    }

    /**
     * 範囲指定で月別サマリーを取得するユースケース
     * 
     * 指定された範囲の各月の支出を集計し、MonthlySummary値オブジェクトのリストを作成して返します。
     * 
     * @param startMonth 開始月（YYYY-MM形式）
     * @param endMonth   終了月（YYYY-MM形式）
     * @return 月別サマリー値オブジェクトのリスト
     */
    @Transactional(readOnly = true)
    public List<MonthlySummary> getMonthlySummaryRange(String startMonth, String endMonth) {
        YearMonth start = parseMonth(startMonth);
        YearMonth end = parseMonth(endMonth);

        if (start.isAfter(end)) {
            throw new IllegalArgumentException("開始月は終了月以前でなければなりません。");
        }

        User user = userApplicationService.getUser();

        List<MonthlySummary> summaries = new ArrayList<>();
        YearMonth current = start;
        while (!current.isAfter(end)) {
            LocalDate monthStart = current.atDay(1);
            LocalDate monthEnd = current.atEndOfMonth();

            List<Expense> expenses = expenseRepository.findByUserAndDateBetween(user, monthStart, monthEnd);

            MonthlySummary summary = MonthlySummary.from(expenses);
            summaries.add(summary);

            current = current.plusMonths(1);
        }

        return summaries;
    }

    /**
     * 利用可能な月のリストを取得するユースケース
     * 
     * 現在のユーザーが支出データを持つ月のリストを取得します。
     * H2とMySQLの両方で動作するように、Javaコードで月をフォーマットします。
     * 
     * @return 利用可能な月のリスト（YYYY-MM形式、降順でソート済み）
     */
    @Transactional(readOnly = true)
    public List<String> getAvailableMonths() {
        User user = userApplicationService.getUser();

        List<LocalDate> distinctDates = expenseRepository.findDistinctDatesByUser(user);

        return distinctDates.stream()
                .map(date -> YearMonth.from(date).format(DateTimeFormatter.ofPattern(MONTH_FORMAT)))
                .distinct()
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());
    }
}
