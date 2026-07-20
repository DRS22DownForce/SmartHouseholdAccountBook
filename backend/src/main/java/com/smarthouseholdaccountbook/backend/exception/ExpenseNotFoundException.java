package com.smarthouseholdaccountbook.backend.exception;

import org.springframework.http.HttpStatus;

/**
 * 支出が見つからない場合の例外
 * 404 Not Foundのステータスコードを返す
 */
public class ExpenseNotFoundException extends BusinessException {
    /**
     * @param id 見つからない支出のID　
     */
    public ExpenseNotFoundException(Long id) {
        super(HttpStatus.NOT_FOUND, "ID: " + id + " の支出が見つかりませんでした。");
    }
}
