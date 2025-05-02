package com.example.backend.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

/**
 * 支出登録用フォームDTO
 * 入力値の形式的なバリデーションを担当
 * Lombokの@Dataでgetter/setter等を自動生成
 */
@Data // getter/setter, toString, equals, hashCode, コンストラクタを自動生成
public class ExpenseForm {
    @NotNull(message = "日付は必須です")
    @PastOrPresent(message = "日付は今日以前の日付を入力してください")
    private LocalDate date;

    @NotBlank(message = "カテゴリーは必須です")
    private String category;

    @NotBlank(message = "説明は必須です")
    private String description;

    @NotNull(message = "金額は必須です")
    @Min(value = 0, message = "金額は0円以上で入力してください")
    private Integer amount;
}