package com.example.backend.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * HomeControllerのユニットテストクラス
 * 外部依存がないため、メソッド戻り値を直接検証する。
 */
class HomeControllerTest {

    private final HomeController homeController = new HomeController();

    @Test
    @DisplayName("homeは歓迎メッセージを返す")
    void returnsWelcomeMessage() {
        String response = homeController.home();

        assertThat(response).isEqualTo("Welcome to Smart Household Account Book API");
    }
}

