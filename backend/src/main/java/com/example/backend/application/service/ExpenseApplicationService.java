package com.example.backend.application.service;

import com.example.backend.application.mapper.ExpenseMapper;
import com.example.backend.domain.repository.ExpenseRepository;
import com.example.backend.domain.valueobject.MonthlySummary;
import com.example.backend.entity.Expense;
import com.example.backend.entity.User;
import com.example.backend.generated.model.ExpenseDto;
import com.example.backend.generated.model.ExpenseRequestDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.backend.exception.ExpenseNotFoundException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 支出に関するアプリケーションサービス
 * このクラスは支出の追加、取得、更新、削除というユースケースを実装します。
 */
@Service
@Transactional
public class ExpenseApplicationService {
    private static final Logger logger = LoggerFactory.getLogger(ExpenseApplicationService.class);

    private final ExpenseRepository expenseRepository;
    private final ExpenseMapper expenseMapper;
    private final UserApplicationService userApplicationService;

    /**
     * コンストラクタ
     * 
     * @param expenseRepository      支出リポジトリ
     * @param expenseMapper          支出マッパー
     * @param userApplicationService ユーザーアプリケーションサービス
     */
    public ExpenseApplicationService(
            ExpenseRepository expenseRepository,
            ExpenseMapper expenseMapper,
            UserApplicationService userApplicationService) {
        this.expenseRepository = expenseRepository;
        this.expenseMapper = expenseMapper;
        this.userApplicationService = userApplicationService;
    }

    /**
     * 全ての支出を取得するユースケース
     * 
     * 現在のユーザーの支出を取得し、DTOに変換して返します。
     * 
     * @return 支出DTOリスト
     */
    @Transactional(readOnly = true)
    public List<ExpenseDto> getExpenses() {
        User user = Objects.requireNonNull(
                userApplicationService.getUser(),
                "ユーザー情報の取得に失敗しました");

        List<Expense> expenses = expenseRepository.findByUser(user);

        return expenses.stream()
                .map(expenseMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 新しい支出を追加するユースケース
     * 
     * リクエストDTOからエンティティを作成し、保存してDTOに変換して返します。
     * 
     * @param expenseRequestDto 支出リクエストDTO
     * @return 追加した支出DTO
     */
    public ExpenseDto addExpense(ExpenseRequestDto expenseRequestDto) {
        Objects.requireNonNull(expenseRequestDto, "支出リクエストDTOはnullであってはなりません");

        User user = Objects.requireNonNull(
                userApplicationService.getUser(),
                "ユーザー情報の取得に失敗しました");

        Expense expense = Objects.requireNonNull(
                expenseMapper.toEntity(expenseRequestDto, user),
                "エンティティの生成に失敗しました");

        Expense savedExpense = expenseRepository.save(expense);

        return expenseMapper.toDto(savedExpense);
    }

    /**
     * 支出を削除するユースケース
     * 
     * 指定されたIDの支出を削除します。
     * 
     * @param id 支出ID
     */
    public void deleteExpense(Long id) {
        Objects.requireNonNull(id, "支出IDはnullであってはなりません");

        expenseRepository.deleteById(id);
    }

    /**
     * 支出を更新するユースケース
     * 
     * 既存の支出を取得し、リクエストDTOの内容で更新します。
     * 
     * @param id                支出ID
     * @param expenseRequestDto 更新する支出リクエストDTO
     * @return 更新された支出DTO
     */
    public ExpenseDto updateExpense(Long id, ExpenseRequestDto expenseRequestDto) {
        Objects.requireNonNull(expenseRequestDto, "支出リクエストDTOはnullであってはなりません");
        Objects.requireNonNull(id, "支出IDはnullであってはなりません");

        Expense existingExpense = expenseRepository.findById(id)
                .orElseThrow(() -> new ExpenseNotFoundException(id));

        Expense.ExpenseUpdate update = expenseMapper
                .toExpenseUpdate(expenseRequestDto);

        existingExpense.update(update);

        Expense savedExpense = expenseRepository.save(existingExpense);
        return expenseMapper.toDto(savedExpense);
    }

    /**
     * 月別支出を取得するユースケース（ページネーション対応）
     * 
     * 指定された月の支出を取得し、DTOに変換して返します。
     * H2とMySQLの両方で動作するように、日付範囲を使用してクエリします。
     * 
     * @param month    月（YYYY-MM形式）
     * @param pageable ページネーション情報
     * @return 支出DTOページ
     */
    @Transactional(readOnly = true)
    public Page<ExpenseDto> getExpensesByMonth(String month, Pageable pageable) {
        Objects.requireNonNull(month, "月はnullであってはなりません");
        YearMonth yearMonth = parseAndValidateMonth(month);

        User user = Objects.requireNonNull(
                userApplicationService.getUser(),
                "ユーザー情報の取得に失敗しました");

        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        Page<Expense> expensePage = expenseRepository.findByUserAndDateRange(user, startDate, endDate, pageable);

        return expensePage.map(expenseMapper::toDto);
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
        Objects.requireNonNull(month, "月はnullであってはなりません");
        YearMonth yearMonth = parseAndValidateMonth(month);

        User user = Objects.requireNonNull(
                userApplicationService.getUser(),
                "ユーザー情報の取得に失敗しました");

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
        Objects.requireNonNull(startMonth, "開始月はnullであってはなりません");
        Objects.requireNonNull(endMonth, "終了月はnullであってはなりません");
        YearMonth start = parseAndValidateMonth(startMonth);
        YearMonth end = parseAndValidateMonth(endMonth);

        if (start.isAfter(end)) {
            throw new IllegalArgumentException("開始月は終了月以前でなければなりません。");
        }

        User user = Objects.requireNonNull(
                userApplicationService.getUser(),
                "ユーザー情報の取得に失敗しました");

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
        User user = Objects.requireNonNull(
                userApplicationService.getUser(),
                "ユーザー情報の取得に失敗しました");

        List<LocalDate> distinctDates = expenseRepository.findDistinctDatesByUser(user);

        return distinctDates.stream()
                .map(date -> YearMonth.from(date).format(DateTimeFormatter.ofPattern("yyyy-MM")))
                .distinct()
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());
    }

    /**
     * 月の形式を検証する
     * 
     * @param month 月（YYYY-MM形式）
     * @return YearMonthオブジェクト
     * @throws IllegalArgumentException 月の形式が不正な場合
     */
    private YearMonth parseAndValidateMonth(String month) {
        if (!month.matches("^\\d{4}-\\d{2}$")) {
            throw new IllegalArgumentException("月の形式が不正です。YYYY-MM形式で指定してください。");
        }
        return YearMonth.parse(month, DateTimeFormatter.ofPattern("yyyy-MM"));
    }
}
