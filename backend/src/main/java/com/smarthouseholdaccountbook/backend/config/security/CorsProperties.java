package com.example.backend.config.security;

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
 */
@Component
@ConfigurationProperties(prefix = "cors")
@Validated
@Getter
@Setter
public class CorsProperties {

    /**
     * 許可するオリジンのリスト
     */
    @NotEmpty(message = "許可するオリジンは必須です")
    private List<String> allowedOrigins;

    /**
     * 許可するHTTPメソッドのリスト
     * デフォルト値: GET, POST, PUT, DELETE, OPTIONS
     * OPTIONSはプリフライトリクエスト（本当のリクエストの前の疎通確認用の下見リクエスト）に必要。
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
     * 認証情報（Cookie、HTTP認証、TLSクライアント証明書）を送信可能にするか。
     * JWT認証を利用するため、falseにする。
     */
    private boolean allowCredentials = false;

    /**
     * プリフライトリクエストの結果をキャッシュする時間（秒）
     * デフォルト値: 3600秒（1時間）
     * ブラウザはこの時間内に同一のリクエストを送信する場合、プリフライトリクエストを送信しない。
     * パフォーマンス向上に役立つ。
     */
    private long maxAge = 3600L;
}

