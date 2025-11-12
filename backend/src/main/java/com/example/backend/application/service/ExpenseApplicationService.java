package com.example.backend.application.service;

import com.example.backend.application.mapper.ExpenseMapper;
import com.example.backend.domain.repository.ExpenseRepository;
import com.example.backend.domain.valueobject.MonthlySummary;
import com.example.backend.entity.Expense;
import com.example.backend.entity.User;
import com.example.backend.generated.model.ExpenseDto;
import com.example.backend.generated.model.ExpenseRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final ExpenseRepository expenseRepository;
    private final ExpenseMapper expenseMapper;
    private final UserApplicationService userApplicationService;

    /**
     * コンストラクタ
     * 
     * @param expenseRepository 支出リポジトリ
     * @param expenseMapper 支出マッパー
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
        // 1. 現在のユーザーを取得
        User user = Objects.requireNonNull(
            userApplicationService.getUser(),
            "ユーザー情報の取得に失敗しました"
        );
        
        // 2. ユーザーの支出を取得
        List<Expense> expenses = expenseRepository.findByUser(user);
        
        // 3. DTOに変換して返す
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
        // 1. 入力検証
        Objects.requireNonNull(expenseRequestDto, "支出リクエストDTOはnullであってはなりません");
        
        // 2. 現在のユーザーを取得
        User user = Objects.requireNonNull(
            userApplicationService.getUser(),
            "ユーザー情報の取得に失敗しました"
        );
        
        // 3. DTOからエンティティへ変換（マッパーが値オブジェクトを作成）
        Expense expense = Objects.requireNonNull(
            expenseMapper.toEntity(expenseRequestDto, user),
            "エンティティの生成に失敗しました"
        );
        
        // 4. データベースに保存
        Expense savedExpense = expenseRepository.save(expense);
        
        // 5. DTOに変換して返す
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
        // 1. 入力検証
        Objects.requireNonNull(id, "支出IDはnullであってはなりません");
        
        // 2. データベースから削除
        expenseRepository.deleteById(id);
    }

    /**
     * 支出を更新するユースケース
     * 
     * 既存の支出を取得し、リクエストDTOの内容で更新します。
     * 
     * @param id 支出ID
     * @param expenseRequestDto 更新する支出リクエストDTO
     * @return 更新された支出DTO
     */
    public ExpenseDto updateExpense(Long id, ExpenseRequestDto expenseRequestDto) {
        Objects.requireNonNull(expenseRequestDto, "支出リクエストDTOはnullであってはなりません");
        Objects.requireNonNull(id, "支出IDはnullであってはなりません");
        
        Expense existingExpense = expenseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ID " + id + " の支出が見つかりません"));

        ExpenseMapper.ValueObjectsForUpdate valueObjectsForUpdate = 
            expenseMapper.toValueObjectsForUpdate(expenseRequestDto);

        existingExpense.update(
            expenseRequestDto.getDescription(),
            valueObjectsForUpdate.amount(),
            valueObjectsForUpdate.date(),
            valueObjectsForUpdate.category()
        );

        Expense savedExpense = expenseRepository.save(existingExpense);
        return expenseMapper.toDto(savedExpense);
    }

    /**
     * 月別支出を取得するユースケース（ページネーション対応）
     * 
     * 指定された月の支出を取得し、DTOに変換して返します。
     * H2とMySQLの両方で動作するように、日付範囲を使用してクエリします。
     * 
     * @param month 月（YYYY-MM形式）
     * @param pageable ページネーション情報
     * @return 支出DTOページ
     */
    @Transactional(readOnly = true)
    public Page<ExpenseDto> getExpensesByMonth(String month, Pageable pageable) {
        // 1. 入力検証
        Objects.requireNonNull(month, "月はnullであってはなりません");
        validateMonthFormat(month);
        
        // 2. 現在のユーザーを取得
        User user = Objects.requireNonNull(
            userApplicationService.getUser(),
            "ユーザー情報の取得に失敗しました"
        );
        
        // 3. 月の開始日と終了日を計算（H2とMySQLの両方で動作するように）
        YearMonth yearMonth = YearMonth.parse(month, DateTimeFormatter.ofPattern("yyyy-MM"));
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        
        // 4. ユーザーの指定月の支出を取得（ページネーション対応）
        Page<Expense> expensePage = expenseRepository.findByUserAndDateRange(user, startDate, endDate, pageable);
        
        // 5. DTOに変換して返す
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
        // 1. 入力検証
        Objects.requireNonNull(month, "月はnullであってはなりません");
        validateMonthFormat(month);
        
        // 2. 現在のユーザーを取得
        User user = Objects.requireNonNull(
            userApplicationService.getUser(),
            "ユーザー情報の取得に失敗しました"
        );
        
        // 3. 月の開始日と終了日を計算
        YearMonth yearMonth = YearMonth.parse(month, DateTimeFormatter.ofPattern("yyyy-MM"));
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        
        // 4. 指定月の支出を取得
        List<Expense> expenses = expenseRepository.findByUserAndDateBetween(user, startDate, endDate);
        
        // 5. 集計処理（ドメイン層のファクトリーメソッドを使用）
        return MonthlySummary.from(expenses);
    }

    /**
     * 範囲指定で月別サマリーを取得するユースケース
     * 
     * 指定された範囲の各月の支出を集計し、MonthlySummary値オブジェクトのリストを作成して返します。
     * 
     * @param startMonth 開始月（YYYY-MM形式）
     * @param endMonth 終了月（YYYY-MM形式）
     * @return 月別サマリー値オブジェクトのリスト
     */
    @Transactional(readOnly = true)
    public List<MonthlySummary> getMonthlySummaryRange(String startMonth, String endMonth) {
        // 1. 入力検証
        Objects.requireNonNull(startMonth, "開始月はnullであってはなりません");
        Objects.requireNonNull(endMonth, "終了月はnullであってはなりません");
        validateMonthFormat(startMonth);
        validateMonthFormat(endMonth);
        
        // 2. 月の範囲を検証
        YearMonth start = YearMonth.parse(startMonth, DateTimeFormatter.ofPattern("yyyy-MM"));
        YearMonth end = YearMonth.parse(endMonth, DateTimeFormatter.ofPattern("yyyy-MM"));
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("開始月は終了月以前でなければなりません。");
        }
        
        // 3. 現在のユーザーを取得
        User user = Objects.requireNonNull(
            userApplicationService.getUser(),
            "ユーザー情報の取得に失敗しました"
        );
        
        // 4. 範囲内の各月のサマリーを計算
        List<MonthlySummary> summaries = new ArrayList<>();
        YearMonth current = start;
        while (!current.isAfter(end)) {
            LocalDate monthStart = current.atDay(1);
            LocalDate monthEnd = current.atEndOfMonth();
            
            // 指定月の支出を取得
            List<Expense> expenses = expenseRepository.findByUserAndDateBetween(user, monthStart, monthEnd);
            
            // 集計処理（ドメイン層のファクトリーメソッドを使用）
            MonthlySummary summary = MonthlySummary.from(expenses);
            summaries.add(summary);
            
            // 次の月へ
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
        // 1. 現在のユーザーを取得
        User user = Objects.requireNonNull(
            userApplicationService.getUser(),
            "ユーザー情報の取得に失敗しました"
        );
        
        // 2. ユーザーの支出がある日付のリストを取得
        List<LocalDate> distinctDates = expenseRepository.findDistinctDatesByUser(user);
        
        // 3. 日付から月（YYYY-MM形式）を抽出し、重複を除去してソート
        // YearMonthを使用して月を抽出し、文字列にフォーマット
        return distinctDates.stream()
            .map(date -> YearMonth.from(date).format(DateTimeFormatter.ofPattern("yyyy-MM")))
            .distinct() // 同じ月が複数回出現する可能性があるため、重複を除去
            .sorted(Comparator.reverseOrder()) // 降順でソート
            .collect(Collectors.toList());
    }

    /**
     * 月の形式を検証する
     * 
     * @param month 月（YYYY-MM形式）
     * @throws IllegalArgumentException 月の形式が不正な場合
     */
    private void validateMonthFormat(String month) {
        if (!month.matches("^\\d{4}-\\d{2}$")) {
            throw new IllegalArgumentException("月の形式が不正です。YYYY-MM形式で指定してください。");
        }
        try {
            YearMonth.parse(month, DateTimeFormatter.ofPattern("yyyy-MM"));
        } catch (Exception e) {
            throw new IllegalArgumentException("月の形式が不正です。YYYY-MM形式で指定してください。", e);
        }
    }
}

