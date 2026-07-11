package com.smarthouseholdaccountbook.backend.application.service;

import com.smarthouseholdaccountbook.backend.entity.Expense;
import com.smarthouseholdaccountbook.backend.entity.MonthlyReport;
import com.smarthouseholdaccountbook.backend.entity.User;
import com.smarthouseholdaccountbook.backend.repository.ExpenseRepository;
import com.smarthouseholdaccountbook.backend.repository.MonthlyReportRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * 月次 AI レポートの DB アクセス専用サービス。
 *
 * OpenAI 呼び出しとは TX を分離し、読み取り・保存それぞれ短い TX のみを張る。
 */
@Service
@Transactional
public class MonthlyReportPersistenceService {

    private static final String MONTH_FORMAT = "yyyy-MM";

    private final ExpenseRepository expenseRepository;
    private final MonthlyReportRepository monthlyReportRepository;

    public MonthlyReportPersistenceService(
            ExpenseRepository expenseRepository,
            MonthlyReportRepository monthlyReportRepository) {
        this.expenseRepository = expenseRepository;
        this.monthlyReportRepository = monthlyReportRepository;
    }

    /**
     * キャッシュ済みレポートを取得する（再生成しない場合）。
     */
    @Transactional(readOnly = true)
    public Optional<MonthlyReport> findExisting(User user, String month) {
        return monthlyReportRepository.findByUserAndReportMonth(user, month);
    }

    /**
     * レポート生成に必要な支出データを読み取る。
     */
    @Transactional(readOnly = true)
    public List<Expense> loadExpensesForGeneration(User user, String month) {
        YearMonth yearMonth = YearMonth.parse(month, DateTimeFormatter.ofPattern(MONTH_FORMAT));
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        return expenseRepository.findByUserAndDateBetween(user, startDate, endDate);
    }

    /**
     * AI 生成結果を新規保存または更新する。
     *
     * <p>既存レポートはこの書き込みトランザクション内で取得します。したがって、既存の
     * エンティティは managed 状態となり、{@link MonthlyReport#update(String, List)} の変更が
     * コミット時に DB へ反映されます。</p>
     */
    public MonthlyReport saveOrUpdate(
            User user,
            String month,
            String summary,
            List<String> suggestions) {
        return monthlyReportRepository.findByUserAndReportMonth(user, month)
                .map(existingReport -> {
                    existingReport.update(summary, suggestions);
                    return existingReport;
                })
                .orElseGet(() -> monthlyReportRepository.save(
                        new MonthlyReport(user, month, summary, suggestions)));
    }

}
