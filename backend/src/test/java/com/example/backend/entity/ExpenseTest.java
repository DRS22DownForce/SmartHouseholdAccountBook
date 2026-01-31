package com.example.backend.entity;

import com.example.backend.domain.valueobject.CategoryType;
import com.example.backend.domain.valueobject.ExpenseAmount;
import com.example.backend.domain.valueobject.ExpenseDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Expenseエンティティのテストクラス
 * 
 * 支出エンティティのバリデーションと変更メソッドをテストします。
 * エンティティは識別子（ID）を持ち、状態が変更可能です。
 */
class ExpenseTest {

    @Test
    @DisplayName("正常な支出エンティティを作成できる")
    void createExpense_正常な値() {
        // テストデータの準備: 必要な値オブジェクトとユーザーを作成
        User user = new User("cognitoSub", "test@example.com");
        ExpenseAmount amount = new ExpenseAmount(1000);
        ExpenseDate date = new ExpenseDate(LocalDate.now());
        CategoryType category = CategoryType.FOOD;
        String description = "テスト支出";

        // テスト実行: Expenseエンティティを作成
        Expense expense = new Expense(description, amount, date, category, user);

        // 検証: 正常に作成され、値が正しく設定されていることを確認
        assertNotNull(expense);
        assertEquals("テスト支出", expense.getDescription());
        assertEquals(1000, expense.getAmount().toInteger());
        assertEquals(LocalDate.now(), expense.getDate().toLocalDate());
        assertEquals("食費", expense.getCategory().getDisplayName());
        assertEquals(user, expense.getUser());
    }

    @Test
    @DisplayName("説明がnullなら例外")
    void createExpense_説明がnullなら例外() {
        // テストデータの準備
        User user = new User("cognitoSub", "test@example.com");
        ExpenseAmount amount = new ExpenseAmount(1000);
        ExpenseDate date = new ExpenseDate(LocalDate.now());
        CategoryType category = CategoryType.FOOD;

        // テスト実行と検証: nullの説明を渡すと例外が発生することを確認
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new Expense(null, amount, date, category, user));

        assertEquals("説明は必須です。", exception.getMessage());
    }

    @Test
    @DisplayName("説明が空文字列なら例外")
    void createExpense_説明が空文字列なら例外() {
        // テストデータの準備
        User user = new User("cognitoSub", "test@example.com");
        ExpenseAmount amount = new ExpenseAmount(1000);
        ExpenseDate date = new ExpenseDate(LocalDate.now());
        CategoryType category = CategoryType.FOOD;

        // テスト実行と検証
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new Expense("", amount, date, category, user));

        assertEquals("説明は必須です。", exception.getMessage());
    }

    @Test
    @DisplayName("説明が空白のみなら例外")
    void createExpense_説明が空白のみなら例外() {
        // テストデータの準備
        User user = new User("cognitoSub", "test@example.com");
        ExpenseAmount amount = new ExpenseAmount(1000);
        ExpenseDate date = new ExpenseDate(LocalDate.now());
        CategoryType category = CategoryType.FOOD;

        // テスト実行と検証
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new Expense("   ", amount, date, category, user));

        assertEquals("説明は必須です。", exception.getMessage());
    }

    @Test
    @DisplayName("金額がnullなら例外")
    void createExpense_金額がnullなら例外() {
        // テストデータの準備
        User user = new User("cognitoSub", "test@example.com");
        ExpenseDate date = new ExpenseDate(LocalDate.now());
        CategoryType category = CategoryType.FOOD;

        // テスト実行と検証（Objects.requireNonNullはNullPointerExceptionをスロー）
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> new Expense("テスト支出", null, date, category, user));

        assertEquals("金額は必須です。", exception.getMessage());
    }

    @Test
    @DisplayName("日付がnullなら例外")
    void createExpense_日付がnullなら例外() {
        // テストデータの準備
        User user = new User("cognitoSub", "test@example.com");
        ExpenseAmount amount = new ExpenseAmount(1000);
        CategoryType category = CategoryType.FOOD;

        // テスト実行と検証（Objects.requireNonNullはNullPointerExceptionをスロー）
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> new Expense("テスト支出", amount, null, category, user));

        assertEquals("日付は必須です。", exception.getMessage());
    }

    @Test
    @DisplayName("カテゴリーがnullなら例外")
    void createExpense_カテゴリーがnullなら例外() {
        // テストデータの準備
        User user = new User("cognitoSub", "test@example.com");
        ExpenseAmount amount = new ExpenseAmount(1000);
        ExpenseDate date = new ExpenseDate(LocalDate.now());

        // テスト実行と検証（Objects.requireNonNullはNullPointerExceptionをスロー）
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> new Expense("テスト支出", amount, date, null, user));

        assertEquals("カテゴリーは必須です。", exception.getMessage());
    }

    @Test
    @DisplayName("ユーザーがnullなら例外")
    void createExpense_ユーザーがnullなら例外() {
        // テストデータの準備
        ExpenseAmount amount = new ExpenseAmount(1000);
        ExpenseDate date = new ExpenseDate(LocalDate.now());
        CategoryType category = CategoryType.FOOD;

        // テスト実行と検証（Objects.requireNonNullはNullPointerExceptionをスロー）
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> new Expense("テスト支出", amount, date, category, null));

        assertEquals("ユーザーは必須です。", exception.getMessage());
    }

    @Test
    @DisplayName("説明を変更できる")
    void changeDescription_正常に変更() {
        // テストデータの準備: 支出エンティティを作成
        User user = new User("cognitoSub", "test@example.com");
        ExpenseAmount amount = new ExpenseAmount(1000);
        ExpenseDate date = new ExpenseDate(LocalDate.now());
        CategoryType category = CategoryType.FOOD;
        Expense expense = new Expense("元の説明", amount, date, category, user);

        // テスト実行: 説明を変更
        expense.changeDescription("新しい説明");

        // 検証: 説明が正しく変更されていることを確認
        assertEquals("新しい説明", expense.getDescription());
    }

    @Test
    @DisplayName("説明をnullに変更しようとすると例外")
    void changeDescription_nullなら例外() {
        // テストデータの準備
        User user = new User("cognitoSub", "test@example.com");
        ExpenseAmount amount = new ExpenseAmount(1000);
        ExpenseDate date = new ExpenseDate(LocalDate.now());
        CategoryType category = CategoryType.FOOD;
        Expense expense = new Expense("元の説明", amount, date, category, user);

        // テスト実行と検証
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> expense.changeDescription(null));

        assertEquals("説明は必須です。", exception.getMessage());
    }

    @Test
    @DisplayName("説明を空文字列に変更しようとすると例外")
    void changeDescription_空文字列なら例外() {
        // テストデータの準備
        User user = new User("cognitoSub", "test@example.com");
        ExpenseAmount amount = new ExpenseAmount(1000);
        ExpenseDate date = new ExpenseDate(LocalDate.now());
        CategoryType category = CategoryType.FOOD;
        Expense expense = new Expense("元の説明", amount, date, category, user);

        // テスト実行と検証
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> expense.changeDescription(""));

        assertEquals("説明は必須です。", exception.getMessage());
    }

    @Test
    @DisplayName("金額を変更できる")
    void changeAmount_正常に変更() {
        // テストデータの準備
        User user = new User("cognitoSub", "test@example.com");
        ExpenseAmount originalAmount = new ExpenseAmount(1000);
        ExpenseDate date = new ExpenseDate(LocalDate.now());
        CategoryType category = CategoryType.FOOD;
        Expense expense = new Expense("テスト支出", originalAmount, date, category, user);

        // テスト実行: 金額を変更
        ExpenseAmount newAmount = new ExpenseAmount(2000);
        expense.changeAmount(newAmount);

        // 検証: 金額が正しく変更されていることを確認
        assertEquals(2000, expense.getAmount().toInteger());
    }

    @Test
    @DisplayName("金額をnullに変更しようとすると例外")
    void changeAmount_nullなら例外() {
        // テストデータの準備
        User user = new User("cognitoSub", "test@example.com");
        ExpenseAmount amount = new ExpenseAmount(1000);
        ExpenseDate date = new ExpenseDate(LocalDate.now());
        CategoryType category = CategoryType.FOOD;
        Expense expense = new Expense("テスト支出", amount, date, category, user);

        // テスト実行と検証（Objects.requireNonNullはNullPointerExceptionをスロー）
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> expense.changeAmount(null));

        assertEquals("金額は必須です。", exception.getMessage());
    }

    @Test
    @DisplayName("日付を変更できる")
    void changeDate_正常に変更() {
        // テストデータの準備
        User user = new User("cognitoSub", "test@example.com");
        ExpenseAmount amount = new ExpenseAmount(1000);
        ExpenseDate originalDate = new ExpenseDate(LocalDate.now().minusDays(5));
        CategoryType category = CategoryType.FOOD;
        Expense expense = new Expense("テスト支出", amount, originalDate, category, user);

        // テスト実行: 日付を変更
        ExpenseDate newDate = new ExpenseDate(LocalDate.now().minusDays(1));
        expense.changeDate(newDate);

        // 検証: 日付が正しく変更されていることを確認
        assertEquals(LocalDate.now().minusDays(1), expense.getDate().toLocalDate());
    }

    @Test
    @DisplayName("日付をnullに変更しようとすると例外")
    void changeDate_nullなら例外() {
        // テストデータの準備
        User user = new User("cognitoSub", "test@example.com");
        ExpenseAmount amount = new ExpenseAmount(1000);
        ExpenseDate date = new ExpenseDate(LocalDate.now());
        CategoryType category = CategoryType.FOOD;
        Expense expense = new Expense("テスト支出", amount, date, category, user);

        // テスト実行と検証（Objects.requireNonNullはNullPointerExceptionをスロー）
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> expense.changeDate(null));

        assertEquals("日付は必須です。", exception.getMessage());
    }

    @Test
    @DisplayName("カテゴリーを変更できる")
    void changeCategory_正常に変更() {
        // テストデータの準備
        User user = new User("cognitoSub", "test@example.com");
        ExpenseAmount amount = new ExpenseAmount(1000);
        ExpenseDate date = new ExpenseDate(LocalDate.now());
        CategoryType originalCategory = CategoryType.FOOD;
        Expense expense = new Expense("テスト支出", amount, date, originalCategory, user);

        // テスト実行: カテゴリーを変更
        CategoryType newCategory = CategoryType.TRANSPORT;
        expense.changeCategory(newCategory);

        // 検証: カテゴリーが正しく変更されていることを確認
        assertEquals("交通費", expense.getCategory().getDisplayName());
    }

    @Test
    @DisplayName("カテゴリーをnullに変更しようとすると例外")
    void changeCategory_nullなら例外() {
        // テストデータの準備
        User user = new User("cognitoSub", "test@example.com");
        ExpenseAmount amount = new ExpenseAmount(1000);
        ExpenseDate date = new ExpenseDate(LocalDate.now());
        CategoryType category = CategoryType.FOOD;
        Expense expense = new Expense("テスト支出", amount, date, category, user);

        // テスト実行と検証（Objects.requireNonNullはNullPointerExceptionをスロー）
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> expense.changeCategory(null));

        assertEquals("カテゴリーは必須です。", exception.getMessage());
    }

    @Test
    @DisplayName("複数のフィールドを一度に更新できる")
    void update_正常に更新() {
        // テストデータの準備
        User user = new User("cognitoSub", "test@example.com");
        ExpenseAmount originalAmount = new ExpenseAmount(1000);
        ExpenseDate originalDate = new ExpenseDate(LocalDate.now().minusDays(5));
        CategoryType originalCategory = CategoryType.FOOD;
        Expense expense = new Expense("元の説明", originalAmount, originalDate, originalCategory, user);

        // テスト実行: 複数のフィールドを一度に更新
        ExpenseAmount newAmount = new ExpenseAmount(2000);
        ExpenseDate newDate = new ExpenseDate(LocalDate.now().minusDays(1));
        CategoryType newCategory = CategoryType.TRANSPORT;
        expense.update("新しい説明", newAmount, newDate, newCategory);

        // 検証: すべてのフィールドが正しく更新されていることを確認
        assertEquals("新しい説明", expense.getDescription());
        assertEquals(2000, expense.getAmount().toInteger());
        assertEquals(LocalDate.now().minusDays(1), expense.getDate().toLocalDate());
        assertEquals("交通費", expense.getCategory().getDisplayName());
    }

    @Test
    @DisplayName("updateメソッドでnullを渡すとそのフィールドは更新されない")
    void update_nullの場合は更新されない() {
        // テストデータの準備
        User user = new User("cognitoSub", "test@example.com");
        ExpenseAmount originalAmount = new ExpenseAmount(1000);
        ExpenseDate originalDate = new ExpenseDate(LocalDate.now().minusDays(5));
        CategoryType originalCategory = CategoryType.FOOD;
        Expense expense = new Expense("元の説明", originalAmount, originalDate, originalCategory, user);

        // テスト実行: nullを渡すとそのフィールドは更新されない
        expense.update(null, null, null, null);

        // 検証: すべてのフィールドが元の値のままであることを確認
        assertEquals("元の説明", expense.getDescription());
        assertEquals(1000, expense.getAmount().toInteger());
        assertEquals(LocalDate.now().minusDays(5), expense.getDate().toLocalDate());
        assertEquals("食費", expense.getCategory().getDisplayName());
    }

    @Test
    @DisplayName("updateメソッドで空文字列を渡すと説明は更新されない")
    void update_空文字列の場合は更新されない() {
        // テストデータの準備
        User user = new User("cognitoSub", "test@example.com");
        ExpenseAmount amount = new ExpenseAmount(1000);
        ExpenseDate date = new ExpenseDate(LocalDate.now());
        CategoryType category = CategoryType.FOOD;
        Expense expense = new Expense("元の説明", amount, date, category, user);

        // テスト実行: 空文字列を渡すと説明は更新されない
        expense.update("", null, null, null);

        // 検証: 説明が元の値のままであることを確認
        assertEquals("元の説明", expense.getDescription());
    }
}

