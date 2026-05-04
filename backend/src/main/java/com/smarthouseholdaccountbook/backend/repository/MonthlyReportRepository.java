package com.smarthouseholdaccountbook.backend.repository;

import com.smarthouseholdaccountbook.backend.entity.MonthlyReport;
import com.smarthouseholdaccountbook.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 月次AIレポートエンティティのリポジトリインターフェース
 */
public interface MonthlyReportRepository extends JpaRepository<MonthlyReport, Long> {

    /**
     * ユーザーと対象月を指定してレポートを取得する
     *
     * @param user  ユーザーエンティティ
     * @param reportMonth 対象月（YYYY-MM形式）
     * @return 月次レポート（存在しない場合は空）
     */
    Optional<MonthlyReport> findByUserAndReportMonth(User user, String reportMonth);
}
