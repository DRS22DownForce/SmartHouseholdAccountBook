package com.smarthouseholdaccountbook.backend.application.mapper;

import com.smarthouseholdaccountbook.backend.generated.model.MonthlySummaryDto;
import com.smarthouseholdaccountbook.backend.generated.model.MonthlySummaryDtoByCategoryInner;
import com.smarthouseholdaccountbook.backend.valueobject.CategorySummary;
import com.smarthouseholdaccountbook.backend.valueobject.MonthlySummary;
import org.springframework.stereotype.Component;

/**
 * 月別サマリーを API レスポンスへ変換するマッパー。
 */
@Component
public class MonthlySummaryMapper {

    /**
     * 月別サマリーの値オブジェクトを DTO へ変換する。
     *
     * @param summary 月別サマリー
     * @return API レスポンス DTO。引数が {@code null} の場合は {@code null}
     */
    public MonthlySummaryDto toDto(MonthlySummary summary) {
        if (summary == null) {
            return null;
        }

        MonthlySummaryDto dto = new MonthlySummaryDto();
        dto.setTotal(summary.total());
        dto.setCount(summary.count());
        dto.setByCategory(summary.categorySummaries().stream()
                .map(this::toCategoryDto)
                .toList());
        return dto;
    }

    private MonthlySummaryDtoByCategoryInner toCategoryDto(CategorySummary summary) {
        MonthlySummaryDtoByCategoryInner dto = new MonthlySummaryDtoByCategoryInner();
        dto.setCategory(summary.getDisplayName());
        dto.setAmount(summary.getAmount());
        return dto;
    }
}
