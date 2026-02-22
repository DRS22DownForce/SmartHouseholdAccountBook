package com.example.backend.valueobject;

import org.junit.jupiter.api.Test;

import com.example.backend.valueobject.CategorySummary;
import com.example.backend.valueobject.CategoryType;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * CategorySummary値オブジェクトのテストクラス
 * 
 * 値オブジェクトのバリデーションと不変性をテストします。
 */
class CategorySummaryTest {

    @Nested
    @DisplayName("作成")
    class Create {

        @Test
        @DisplayName("正常なカテゴリーと金額で作成できる")
        void createCategorySummary() {
            // given, when
            CategorySummary categorySummary = new CategorySummary(CategoryType.FOOD, 1000);

            // then
            assertThat(categorySummary).isNotNull();
            assertThat(categorySummary.getCategory()).isEqualTo(CategoryType.FOOD);
            assertThat(categorySummary.getAmount()).isEqualTo(1000);
        }
    }

    @Nested
    @DisplayName("バリデーション")
    class Validation {

        @Test
        @DisplayName("カテゴリーがnullの場合は例外が発生する")
        void validateCategoryNotNull() {
            // given, when, then
            assertThatThrownBy(() -> new CategorySummary(null, 1000))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("カテゴリーはnullであってはなりません。");
        }

        @Test
        @DisplayName("金額がnullの場合は例外が発生する")
        void validateAmountNotNull() {
            // given, when, then
            assertThatThrownBy(() -> new CategorySummary(CategoryType.FOOD, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("金額はnullであってはなりません。");
        }

        @Test
        @DisplayName("金額が1未満の場合は例外が発生する")
        void validateAmountPositive() {
            // given, when, then
            assertThatThrownBy(() -> new CategorySummary(CategoryType.FOOD, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("金額は1以上でなければなりません。");
        }
    }
}

