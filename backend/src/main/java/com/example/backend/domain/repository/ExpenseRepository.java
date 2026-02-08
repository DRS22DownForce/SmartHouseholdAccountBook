package com.example.backend.domain.repository;

import com.example.backend.entity.Expense;
import com.example.backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 支出エンティティのリポジトリインターフェース
 * 
 * DDDの原則に従い、ドメイン層のリポジトリインターフェースとして定義します。
 * Spring Data JPAの命名規則に従ってメソッドを定義します。
 * 
 * データ取得の際はこのメソッドを使用し、別ユーザーのデータは取得できないようにする。
 */
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    /**
     * ユーザーを指定して支出を取得
     * 
     * @param user ユーザーエンティティ
     * @return 該当ユーザーの支出リスト
     */
    List<Expense> findByUser(User user);
    
    /**
     * ユーザーとIDを指定して支出を取得
     * 
     * @param id 支出ID
     * @param user ユーザーエンティティ
     * @return 該当ユーザーの支出（存在しない場合は空）
     */
    Optional<Expense> findByIdAndUser(Long id, User user);

    /**
     * ユーザーと日付範囲を指定して支出を取得
     * 
     * @param user ユーザーエンティティ
     * @param start 開始日（含む）
     * @param end 終了日（含む）
     * @return 該当ユーザーの指定期間内の支出リスト(降順でソート)
     */
    @Query("SELECT e FROM Expense e WHERE e.user = :user AND e.date.value >= :start AND e.date.value <= :end ORDER BY e.date.value DESC")
    List<Expense> findByUserAndDateBetween(
        @Param("user") User user,
        @Param("start") LocalDate start,
        @Param("end") LocalDate end
    );

    /**
     * ユーザーを指定して、利用可能な日付のリストを取得
     * 
     * @param user ユーザーエンティティ
     * @return 利用可能な日付のリスト（降順でソート済み、重複なし）
     */
    @Query("SELECT DISTINCT e.date.value FROM Expense e WHERE e.user = :user ORDER BY e.date.value DESC")
    List<LocalDate> findDistinctDatesByUser(@Param("user") User user);

    /**
     * ユーザーと月を指定して支出を取得（ページネーション対応）
     * 
     * H2とMySQLの両方で動作するように、日付範囲を使用してクエリします。
     * 
     * @param user ユーザーエンティティ
     * @param startDate 月の開始日（含む）
     * @param endDate 月の終了日（含む）
     * @param pageable ページネーション情報
     * @return 該当ユーザーの指定月の支出ページ
     */
    @Query("SELECT e FROM Expense e WHERE e.user = :user AND e.date.value >= :startDate AND e.date.value <= :endDate ORDER BY e.date.value DESC, e.id DESC")
    Page<Expense> findByUserAndDateRange(
        @Param("user") User user,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        Pageable pageable
    );
}

