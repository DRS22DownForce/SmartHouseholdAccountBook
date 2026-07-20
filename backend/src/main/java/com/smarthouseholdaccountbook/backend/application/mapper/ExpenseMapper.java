package com.smarthouseholdaccountbook.backend.application.mapper;

import com.smarthouseholdaccountbook.backend.entity.Expense;
import com.smarthouseholdaccountbook.backend.entity.ExpenseUpdate;
import com.smarthouseholdaccountbook.backend.generated.model.ExpenseDto;
import com.smarthouseholdaccountbook.backend.generated.model.ExpenseRequestDto;
import com.smarthouseholdaccountbook.backend.valueobject.CategoryType;
import com.smarthouseholdaccountbook.backend.valueobject.ExpenseAmount;
import com.smarthouseholdaccountbook.backend.valueobject.ExpenseDate;

import org.springframework.stereotype.Component;

/**
 * 支出 CRUD に関する API DTO とドメイン型の変換を行うマッパー。
 *
 * <p>このクラスは Controller 層専用です。集計、CSV、月次レポートの変換は、
 * それぞれ専用のマッパーへ分離しています。</p>
 */
@Component
public class ExpenseMapper {
    /**
     * エンティティからDTOへ変換
     * 
     * 値オブジェクトからプリミティブ型への変換を行います。
     * 
     * @param expense 支出エンティティ
     * @return 支出DTO（expenseがnullの場合はnull）
     */
    public ExpenseDto toDto(Expense expense) {
        if (expense == null) {
            return null;
        }

        ExpenseDto dto = new ExpenseDto();
        dto.setId(expense.getId());
        dto.setDescription(expense.getDescription());

        // 値オブジェクトからInteger値へ変換
        dto.setAmount(expense.getAmount().getAmount());

        // 値オブジェクトからLocalDate値へ変換
        dto.setDate(expense.getDate().getDate());

        // CategoryTypeから表示名へ変換
        dto.setCategory(expense.getCategory().getDisplayName());

        return dto;
    }

    /**
     * リクエストDTOから更新用の値オブジェクトへの変換
     *
     * 作成・更新はサービス層で Entity を組み立てるため、
     * Controller では DTO → ExpenseUpdate への変換のみ行う。
     *
     * @param dto 支出リクエストDTO
     * @return 更新用の値オブジェクト
     */
    public ExpenseUpdate toExpenseUpdate(ExpenseRequestDto dto) {
        return new ExpenseUpdate(
                dto.getDescription(),
                new ExpenseAmount(dto.getAmount()),
                new ExpenseDate(dto.getDate()),
                CategoryType.fromDisplayName(dto.getCategory()));
    }
}
