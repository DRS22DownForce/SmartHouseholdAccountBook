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
     * レポート生成に必要な支出データと既存レポートを読み取る。
     */
    @Transactional(readOnly = true)
    public ReportGenerationContext loadForGeneration(User user, String month) {
        YearMonth yearMonth = YearMonth.parse(month, DateTimeFormatter.ofPattern(MONTH_FORMAT));
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<Expense> expenses = expenseRepository.findByUserAndDateBetween(user, startDate, endDate);
        Optional<MonthlyReport> existing = monthlyReportRepository.findByUserAndReportMonth(user, month);

        return new ReportGenerationContext(expenses, existing);
    }

    /**
     * AI 生成結果を新規保存または更新する。
     */
    @Transactional
    public MonthlyReport saveOrUpdate(
            User user,
            String month,
            Optional<MonthlyReport> existing,
            String summary,
            List<String> suggestions) {
        if (existing.isPresent()) {
            MonthlyReport entity = existing.get();
            entity.update(summary, suggestions);
            return entity;
        }
        return monthlyReportRepository.save(new MonthlyReport(user, month, summary, suggestions));
    }

    /**
     * レポート生成前に読み取った DB 状態。
     *
     * @param expenses       対象月の支出一覧
     * @param existingReport 既存レポート（再生成時に UPDATE する場合）
     */
    public record ReportGenerationContext(
            List<Expense> expenses,
            Optional<MonthlyReport> existingReport) {
    }
}
