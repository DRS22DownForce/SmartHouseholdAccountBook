package com.example.backend.valueobject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.example.backend.valueobject.CategoryType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * CategoryType Enumのテストクラス
 *
 * 支出カテゴリのEnumのバリデーションをテストします。
 */
class CategoryTypeTest {

    @Nested
    @DisplayName("表示名からEnumを取得（正常系）")
    class FromDisplayNameSuccess {
        @ParameterizedTest // 全てのCategoryTypeをテストする
        @EnumSource(CategoryType.class)
        @DisplayName("正常な表示名からからEnumを取得できる")
        void fromDisplayNameSuccess(CategoryType expected) {
            // given, when
            CategoryType actual = CategoryType.fromDisplayName(expected.getDisplayName());
            
            // then
            assertThat(actual).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("表示名からEnumを取得（異常系）")
    class FromDisplayNameFailure {
        @Test
        @DisplayName("表示名がnullの場合は例外が発生する")
        void fromDisplayNameNull() {
            // given, when, then
            assertThatThrownBy(() -> CategoryType.fromDisplayName(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("カテゴリは必須です。");
            }
        
        @Test
        @DisplayName("表示名が空文字列の場合は例外が発生する")
        void fromDisplayNameEmpty() {
            // given, when, then
            assertThatThrownBy(() -> CategoryType.fromDisplayName(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("カテゴリは必須です。");
        }

        @Test
        @DisplayName("表示名が無効なカテゴリの場合は例外が発生する")
        void fromDisplayNameInvalidCategory() {
            // given, when, then
            assertThatThrownBy(() -> CategoryType.fromDisplayName("無効なカテゴリ"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("無効なカテゴリです。有効なカテゴリ: " + CategoryType.getValidDisplayNames());
        }
    }

}
