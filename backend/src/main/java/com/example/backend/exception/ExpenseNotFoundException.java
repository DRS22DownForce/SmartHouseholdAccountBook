package com.example.backend.exception;

/**
 * 支出が見つからない場合の例外
 * 404 Not Foundのステータスコードを返す
 */
public class ExpenseNotFoundException extends RuntimeException {
    /**
     * @param id 見つからない支出のID
     */
    public ExpenseNotFoundException(Long id) {
        super("ID: " + id + " の支出が見つかりませんでした");
    }
}
