package com.example.backend.service;

import com.example.backend.entity.Expense;
import com.example.backend.entity.User;
import com.example.backend.generated.model.ExpenseDto;
import com.example.backend.generated.model.ExpenseRequestDto;
import com.example.backend.repository.ExpenseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {
    @Mock
    private ExpenseRepository expenseRepository;

    // ExpenseMapperはテスト対象クラスではないのでMock化
    @Mock
    private ExpenseMapper expenseMapper;

    @Mock
    private UserService userService;

    @InjectMocks
    private ExpenseService expenseService;

    @Test
    void getAllExpenses_リポジトリに2件あれば2件返す() {
        User user = new User("cognitoSub", "test@example.com");
        Expense expense1 = new Expense("支出1", 1000, LocalDate.of(2024, 1, 1), "食費", user);
        Expense expense2 = new Expense("支出2", 2000, LocalDate.of(2024, 1, 2), "交通費", user);
        ExpenseDto dto1 = new ExpenseDto();
        dto1.setDescription("支出1");
        dto1.setAmount(1000);
        dto1.setDate(LocalDate.of(2024, 1, 1));
        dto1.setCategory("食費");
        ExpenseDto dto2 = new ExpenseDto();
        dto2.setDescription("支出2");
        dto2.setAmount(2000);
        dto2.setDate(LocalDate.of(2024, 1, 2));
        dto2.setCategory("交通費");

        // テスト用のユーザーを作成
        when(expenseRepository.findByUser(user)).thenReturn(Arrays.asList(expense1, expense2));
        when(expenseMapper.toDto(expense1)).thenReturn(dto1);
        when(expenseMapper.toDto(expense2)).thenReturn(dto2);
        when(userService.getUser()).thenReturn(user);
        List<ExpenseDto> result = expenseService.getExpenses();

        assertEquals(2, result.size());
        assertEquals("支出1", result.get(0).getDescription());
        assertEquals("支出2", result.get(1).getDescription());
    }

    @Test
    void addExpense_支出追加() {
        ExpenseRequestDto requestDto = new ExpenseRequestDto();
        requestDto.setDescription("テスト支出");
        requestDto.setAmount(1000);
        requestDto.setDate(LocalDate.of(2024, 1, 1));
        requestDto.setCategory("食費");

        User user = new User("cognitoSub", "test@example.com");
        Expense expense = new Expense("テスト支出", 1000, LocalDate.of(2024, 1, 1), "食費", user);
        when(expenseMapper.toEntity(requestDto, user)).thenReturn(expense);
        when(userService.getUser()).thenReturn(user);

        ExpenseDto expenseDto = new ExpenseDto();
        expenseDto.setDescription("テスト支出");
        expenseDto.setAmount(1000);
        expenseDto.setDate(LocalDate.of(2024, 1, 1));
        expenseDto.setCategory("食費");
        when(expenseMapper.toDto(expense)).thenReturn(expenseDto);

        when(expenseRepository.save(expense)).thenReturn(expense);

        ExpenseDto result = expenseService.addExpense(requestDto);

        assertNotNull(result);
        assertEquals("テスト支出", result.getDescription());
        assertEquals(1000, result.getAmount());
        assertEquals(LocalDate.of(2024, 1, 1), result.getDate());
        assertEquals("食費", result.getCategory());
        verify(expenseRepository, times(1)).save(expense);
    }

    @Test
    void addExpense_リクエストがnullなら例外() {
        assertThrows(NullPointerException.class, () -> expenseService.addExpense(null));
    }

    @Test
    void updateExpense_正常に更新できる() {
        // テストデータの準備
        Long expenseId = 1L;
        ExpenseRequestDto requestDto = new ExpenseRequestDto();
        requestDto.setDescription("更新された支出");
        requestDto.setAmount(1500);
        requestDto.setDate(LocalDate.of(2024, 1, 15));
        requestDto.setCategory("娯楽費");

        // 既存の支出エンティティをモック
        User user = new User("cognitoSub", "test@example.com");
        Expense existingExpense = new Expense("元の支出", 1000, LocalDate.of(2024, 1, 1), "食費", user);

        // 更新後の支出DTOをモック
        ExpenseDto updatedDto = new ExpenseDto();
        updatedDto.setDescription("更新された支出");
        updatedDto.setAmount(1500);
        updatedDto.setDate(LocalDate.of(2024, 1, 15));
        updatedDto.setCategory("娯楽費");

        // モックの設定
        when(expenseRepository.findById(expenseId)).thenReturn(java.util.Optional.of(existingExpense));
        when(expenseRepository.save(existingExpense)).thenReturn(existingExpense);
        when(expenseMapper.toDto(existingExpense)).thenReturn(updatedDto);

        // テスト実行
        ExpenseDto result = expenseService.updateExpense(expenseId, requestDto);

        // 検証
        assertNotNull(result);
        assertEquals("更新された支出", result.getDescription());
        assertEquals(1500, result.getAmount());
        assertEquals(LocalDate.of(2024, 1, 15), result.getDate());
        assertEquals("娯楽費", result.getCategory());

        // リポジトリのメソッドが正しく呼ばれたことを確認
        verify(expenseRepository, times(1)).findById(expenseId);
        verify(expenseRepository, times(1)).save(existingExpense);
        verify(expenseMapper, times(1)).toDto(existingExpense);
    }

    @Test
    void updateExpense_存在しないIDなら例外() {
        // テストデータの準備
        Long nonExistentId = 999L;
        ExpenseRequestDto requestDto = new ExpenseRequestDto();
        requestDto.setDescription("テスト支出");

        // モックの設定（存在しないIDなので空のOptionalを返す）
        when(expenseRepository.findById(nonExistentId)).thenReturn(java.util.Optional.empty());

        // テスト実行と検証
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> expenseService.updateExpense(nonExistentId, requestDto));

        assertEquals("Expense not found with id: 999", exception.getMessage());
        verify(expenseRepository, times(1)).findById(nonExistentId);
        verify(expenseRepository, never()).save(any());
    }

    @Test
    void updateExpense_リクエストがnullなら例外() {
        // テスト実行と検証
        assertThrows(NullPointerException.class,
                () -> expenseService.updateExpense(1L, null));

        // リポジトリは呼ばれないことを確認
        verify(expenseRepository, never()).findById(any());
        verify(expenseRepository, never()).save(any());
    }
}