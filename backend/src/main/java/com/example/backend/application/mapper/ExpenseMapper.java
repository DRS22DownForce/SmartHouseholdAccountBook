package com.example.backend.application.mapper;

import com.example.backend.domain.valueobject.Category;
import com.example.backend.domain.valueobject.ExpenseAmount;
import com.example.backend.domain.valueobject.ExpenseDate;
import com.example.backend.entity.Expense;
import com.example.backend.entity.User;
import com.example.backend.generated.model.ExpenseDto;
import com.example.backend.generated.model.ExpenseRequestDto;
import org.springframework.stereotype.Component;

/**
 * 支出エンティティとDTO間の変換を行うマッパー
 * 
 * アプリケーション層に配置されるマッパーです。
 * ドメインオブジェクト（エンティティ、値オブジェクト）とDTO間の変換を担当します。
 * 
 * このクラスは以下の責務を持ちます:
 * - エンティティからDTOへの変換
 * - DTOからエンティティへの変換（値オブジェクトの作成を含む）
 * - リクエストDTOから値オブジェクトへの変換（更新用）
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
        dto.setAmount(expense.getAmount() != null ? expense.getAmount().toInteger() : null);
        
        // 値オブジェクトからLocalDate値へ変換
        dto.setDate(expense.getDate() != null ? expense.getDate().toLocalDate() : null);
        
        // 値オブジェクトからString値へ変換
        dto.setCategory(expense.getCategory() != null ? expense.getCategory().getValue() : null);
        
        return dto;
    }

    /**
     * DTOからエンティティへ変換
     * 
     * プリミティブ型から値オブジェクトへの変換を行います。
     * 
     * @param dto 支出DTO
     * @param user ユーザーエンティティ
     * @return 支出エンティティ（dtoがnullの場合はnull）
     */
    public Expense toEntity(ExpenseDto dto, User user) {
        if (dto == null) {
            return null;
        }
        
        // プリミティブ型から値オブジェクトへ変換
        ExpenseAmount amount = dto.getAmount() != null ? new ExpenseAmount(dto.getAmount()) : null;
        ExpenseDate date = dto.getDate() != null ? new ExpenseDate(dto.getDate()) : null;
        Category category = dto.getCategory() != null ? new Category(dto.getCategory()) : null;
        
        return new Expense(
                dto.getDescription(),
                amount,
                date,
                category,
                user);
    }

    /**
     * リクエストDTOからエンティティへ変換
     * 
     * プリミティブ型から値オブジェクトへの変換を行います。
     * 
     * @param dto 支出リクエストDTO
     * @param user ユーザーエンティティ
     * @return 支出エンティティ（dtoがnullの場合はnull）
     */
    public Expense toEntity(ExpenseRequestDto dto, User user) {
        if (dto == null) {
            return null;
        }
        
        // プリミティブ型から値オブジェクトへ変換
        ExpenseAmount amount = dto.getAmount() != null ? new ExpenseAmount(dto.getAmount()) : null;
        ExpenseDate date = dto.getDate() != null ? new ExpenseDate(dto.getDate()) : null;
        Category category = dto.getCategory() != null ? new Category(dto.getCategory()) : null;
        
        return new Expense(
                dto.getDescription(),
                amount,
                date,
                category,
                user);
    }

    /**
     * リクエストDTOから値オブジェクトへの変換（更新用）
     * 
     * @param dto 支出リクエストDTO
     * @return 値オブジェクト（amount, date, category）
     */
    public ValueObjectsForUpdate toValueObjectsForUpdate(ExpenseRequestDto dto) {
        if (dto == null) {
            return new ValueObjectsForUpdate(null, null, null);
        }
        
        ExpenseAmount amount = dto.getAmount() != null 
            ? new ExpenseAmount(dto.getAmount()) 
            : null;
        ExpenseDate date = dto.getDate() != null 
            ? new ExpenseDate(dto.getDate()) 
            : null;
        Category category = dto.getCategory() != null 
            ? new Category(dto.getCategory()) 
            : null;
        
        return new ValueObjectsForUpdate(amount, date, category);
    }

    /**
     * 更新用の値オブジェクトを保持するレコード
     */
    public record ValueObjectsForUpdate(
        ExpenseAmount amount,
        ExpenseDate date,
        Category category
    ) {}
}

