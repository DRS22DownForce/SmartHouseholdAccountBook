package com.example.backend.controller;

import com.example.backend.config.TestSecurityConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * HomeControllerのテストクラス
 * 
 * ホームコントローラーのエンドポイントをテストします。
 * このコントローラーはシンプルなGETエンドポイントを提供します。
 * 
 * @SpringBootTest: アプリケーション全体を起動してテストします
 * @AutoConfigureMockMvc: MockMvcを自動設定します（HTTPリクエストをモック化）
 * @ActiveProfiles("test"): テスト用のプロファイル（application-test.properties）を使用します
 * @Import(TestSecurityConfig.class): テスト用のセキュリティ設定を読み込みます（認証を無効化）
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class HomeControllerTest {

    // MockMvc: Spring MVCのコントローラーをテストするためのモックオブジェクト
    // 実際のHTTPサーバーを起動せずに、コントローラーの動作をテストできます
    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("ホームエンドポイントにアクセスできる")
    void home_正常にアクセスできる() throws Exception {
        // テスト実行: GETリクエストを送信
        // 検証: ステータスコード200（OK）が返され、期待されるメッセージが含まれていることを確認
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string("Welcome to Smart Household Account Book API"));
    }

    @Test
    @DisplayName("ホームエンドポイントはJSONではなくプレーンテキストを返す")
    void home_プレーンテキストを返す() throws Exception {
        // テスト実行と検証: コンテンツタイプがテキストであることを確認
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/plain;charset=UTF-8"));
    }
}

