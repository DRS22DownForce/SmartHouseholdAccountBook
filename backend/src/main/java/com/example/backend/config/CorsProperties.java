package com.example.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

/**
 * CORS設定を管理するプロパティクラス
 * application.propertiesから設定値を読み込みます
 * 
 * このクラスにより、環境ごとに異なるCORS設定を簡単に管理できます
 */
@Component
@ConfigurationProperties(prefix = "cors")
@Validated
@Getter
@Setter
public class CorsProperties {

    /**
     * 許可するオリジンのリスト
     * 環境変数から読み込むことで、環境ごとに異なるオリジンを設定できます
     */
    @NotEmpty(message = "許可するオリジンは必須です")
    private List<String> allowedOrigins;

    /**
     * 許可するHTTPメソッドのリスト
     * デフォルト値: GET, POST, PUT, DELETE, OPTIONS
     * OPTIONSはプリフライトリクエストに必要です
     */
    @NotNull
    private List<String> allowedMethods = List.of("GET", "POST", "PUT", "DELETE", "OPTIONS");

    /**
     * 許可するリクエストヘッダーのリスト
     * セキュリティのため、実際に使用するヘッダーのみを明示的に指定します
     * デフォルト値: Authorization（JWT認証用）, Content-Type（JSON送信用）
     */
    @NotNull
    private List<String> allowedHeaders = List.of(
            "Authorization",
            "Content-Type");

    /**
     * フロントエンドで参照できるレスポンスヘッダーのリスト
     * デフォルト値: Content-Type（JSONレスポンス用）
     */
    @NotNull
    private List<String> exposedHeaders = List.of("Content-Type");

    /**
     * 認証情報（Cookieなど）を送信可能にするか
     * デフォルト値: true
     */
    private boolean allowCredentials = true;

    /**
     * プリフライトリクエストの結果をキャッシュする時間（秒）
     * デフォルト値: 3600秒（1時間）
     * これにより、同じオリジンからのリクエストは指定時間内は再チェックされません
     * パフォーマンス向上に役立ちます
     */
    private long maxAge = 3600L;
}
