package com.example.backend.application.mapper;

import com.example.backend.domain.valueobject.Category;
import com.example.backend.domain.valueobject.CategorySummary;
import com.example.backend.domain.valueobject.ExpenseAmount;
import com.example.backend.domain.valueobject.ExpenseDate;
import com.example.backend.domain.valueobject.MonthlySummary;
import com.example.backend.entity.Expense;
import com.example.backend.entity.User;
import com.example.backend.application.service.CsvExpenseService;
import com.example.backend.application.service.CsvParserService;
import com.example.backend.generated.model.CsvUploadResponseDto;
import com.example.backend.generated.model.CsvUploadResponseDtoErrorsInner;
import com.example.backend.generated.model.ExpenseDto;
import com.example.backend.generated.model.ExpenseRequestDto;
import com.example.backend.generated.model.MonthlySummaryDto;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
 * - Service層の結果オブジェクトからDTOへの変換（CSVアップロード結果など）
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
     * @param dto  支出DTO
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
     * @param dto  支出リクエストDTO
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
            Category category) {
    }

    /**
     * MonthlySummary値オブジェクトからDTOへ変換
     * 
     * DDDの原則に従い、ドメイン層の値オブジェクトをDTOに変換します。
     * 
     * @param monthlySummary 月別サマリー値オブジェクト
     * @return 月別サマリーDTO（monthlySummaryがnullの場合はnull）
     */
    public MonthlySummaryDto toDto(MonthlySummary monthlySummary) {
        if (monthlySummary == null) {
            return null;
        }

        MonthlySummaryDto dto = new MonthlySummaryDto();
        dto.setTotal(monthlySummary.getTotal());
        dto.setCount(monthlySummary.getCount());

        // CategorySummaryのリストをDTOのリストに変換
        List<com.example.backend.generated.model.MonthlySummaryDtoByCategoryInner> byCategoryList = new ArrayList<>();
        if (monthlySummary.getByCategory() != null) {
            for (CategorySummary categorySummary : monthlySummary.getByCategory()) {
                com.example.backend.generated.model.MonthlySummaryDtoByCategoryInner categoryDto = new com.example.backend.generated.model.MonthlySummaryDtoByCategoryInner();
                categoryDto.setCategory(categorySummary.getCategoryValue());
                categoryDto.setAmount(categorySummary.getAmount());
                byCategoryList.add(categoryDto);
            }
        }
        dto.setByCategory(byCategoryList);

        return dto;
    }

    /**
     * CSVアップロード結果からDTOへ変換
     * 
     * @param result CSVアップロード結果（成功件数、エラー件数、エラー詳細を含む）
     * @return CSVアップロード結果DTO（resultがnullの場合はnull）
     */
    public CsvUploadResponseDto toDto(CsvExpenseService.CsvUploadResult result) {
        if (result == null) {
            return null;
        }

        // エラー詳細のリストをDTOのリストに変換
        // CsvParseError（Service層のレコード）からCsvUploadResponseDtoErrorsInner（DTO）へ変換します
        List<CsvUploadResponseDtoErrorsInner> errorDtos = result.errors().stream()
                .map(this::toErrorDto)
                .collect(Collectors.toList());

        // CSVアップロード結果DTOを作成
        // 成功件数、エラー件数、エラー詳細のリストを設定します
        return new CsvUploadResponseDto(
                result.successCount(),
                result.errorCount(),
                errorDtos);
    }

    /**
     * CSV解析エラーからDTOへ変換
     * 
     * Service層のCsvParseErrorをDTOに変換します。
     * 
     * @param error CSV解析エラー（行番号、行内容、エラーメッセージを含む）
     * @return エラー詳細DTO
     */
    private CsvUploadResponseDtoErrorsInner toErrorDto(CsvParserService.CsvParseError error) {
        // エラー詳細DTOを作成
        // 行番号とエラーメッセージはコンストラクタで設定します
        CsvUploadResponseDtoErrorsInner errorDto = new CsvUploadResponseDtoErrorsInner(
                error.lineNumber(),
                error.message());

        // 行内容はsetterで設定します（OpenAPIの生成コードではオプショナルなため）
        errorDto.setLineContent(error.lineContent());

        return errorDto;
    }
}
