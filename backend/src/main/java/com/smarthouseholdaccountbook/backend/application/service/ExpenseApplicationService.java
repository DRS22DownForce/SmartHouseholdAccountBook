package com.smarthouseholdaccountbook.backend.application.service;

import com.smarthouseholdaccountbook.backend.entity.Expense;
import com.smarthouseholdaccountbook.backend.entity.ExpenseUpdate;
import com.smarthouseholdaccountbook.backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.smarthouseholdaccountbook.backend.exception.ExpenseNotFoundException;
import com.smarthouseholdaccountbook.backend.repository.ExpenseRepository;
import com.smarthouseholdaccountbook.backend.valueobject.MonthlySummary;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 支出に関するアプリケーションサービス
 * このクラスは支出の追加、取得、更新、削除というユースケースを実装します。
 */
@Service
@Transactional
public class ExpenseApplicationService {
    private static final String MONTH_FORMAT = "yyyy-MM";
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern(MONTH_FORMAT);

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
     * 新しい支出を追加するユースケース
     *
     * 作成内容（ExpenseUpdate）と現在ユーザーからエンティティを生成し、保存して返します。
     *
     * @param creation 支出の作成内容（説明・金額・日付・カテゴリ）
     * @return 保存後の支出エンティティ
     */
    public Expense addExpense(ExpenseUpdate creation) {
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
     * @param id     支出ID
     * @param update 更新内容（説明・金額・日付・カテゴリ）
     * @return 更新後の支出エンティティ
     */
    public Expense updateExpense(Long id, ExpenseUpdate update) {
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
     * @param yearMonth 対象月
     * @param pageable  ページネーション情報
     * @return 支出エンティティのページ
     */
    @Transactional(readOnly = true)
    public Page<Expense> getExpensesByMonth(YearMonth yearMonth, Pageable pageable) {
        Objects.requireNonNull(yearMonth, "yearMonth はnullであってはなりません。");
        User user = userApplicationService.getUser();

        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        return expenseRepository.findByUserAndDateRange(user, startDate, endDate, pageable);
    }

    /**
     * 月別サマリーを取得するユースケース
     *
     * 指定された月の支出を集計し、MonthlySummary値オブジェクトを作成して返します。
     * 月文字列のパースは API 境界（Controller）で行い、ここでは YearMonth を受け取ります。
     *
     * @param yearMonth 対象月
     * @return 月別サマリー値オブジェクト
     */
    @Transactional(readOnly = true)
    public MonthlySummary getMonthlySummary(YearMonth yearMonth) {
        Objects.requireNonNull(yearMonth, "yearMonth はnullであってはなりません。");

        User user = userApplicationService.getUser();

        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<Expense> expenses = expenseRepository.findByUserAndDateBetween(user, startDate, endDate);

        // MonthlySummary は DTO 向けに String の月を持つため、ここでフォーマットする
        return MonthlySummary.createMonthlySummaryFromExpenses(expenses, yearMonth.format(MONTH_FORMATTER));
    }

    /**
     * 範囲指定で月別サマリーを取得するユースケース
     *
     * 指定された範囲の各月の支出を集計し、MonthlySummary値オブジェクトのリストを作成して返します。
     * 期間全体を1回のクエリで取得し、メモリ上で月ごとにグルーピングします（月数分のN回クエリを避ける）。
     *
     * @param start 開始月
     * @param end   終了月
     * @return 月別サマリー値オブジェクトのリスト
     */
    @Transactional(readOnly = true)
    public List<MonthlySummary> getMonthlySummaryRange(YearMonth start, YearMonth end) {
        Objects.requireNonNull(start, "start はnullであってはなりません。");
        Objects.requireNonNull(end, "end はnullであってはなりません。");

        if (start.isAfter(end)) {
            throw new IllegalArgumentException("開始月は終了月以前でなければなりません。");
        }

        User user = userApplicationService.getUser();

        // 開始月の1日〜終了月の末日を1回だけ取得する（N+1クエリ回避）
        LocalDate rangeStart = start.atDay(1);
        LocalDate rangeEnd = end.atEndOfMonth();
        List<Expense> expenses = expenseRepository.findByUserAndDateBetween(user, rangeStart, rangeEnd);

        // 支出日の YearMonth でグルーピングする
        Map<YearMonth, List<Expense>> expensesByMonth = expenses.stream()
                .collect(Collectors.groupingBy(expense -> YearMonth.from(expense.getDate().getDate())));

        List<MonthlySummary> summaries = new ArrayList<>();
        YearMonth current = start;
        while (!current.isAfter(end)) {
            // 支出がない月は空リストでサマリーを作る（従来どおり月を欠かさない）
            List<Expense> monthlyExpenses = expensesByMonth.getOrDefault(current, List.of());
            summaries.add(MonthlySummary.createMonthlySummaryFromExpenses(
                    monthlyExpenses,
                    current.format(MONTH_FORMATTER)));
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
                .map(date -> YearMonth.from(date).format(MONTH_FORMATTER))
                .distinct()
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());
    }
}
