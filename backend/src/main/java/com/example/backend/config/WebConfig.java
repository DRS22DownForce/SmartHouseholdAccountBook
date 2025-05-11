package com.example.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration // このクラスが設定クラスであることを示します
public class WebConfig {

    // CORSのグローバル設定を行うBeanを定義します
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                // すべてのAPIパスに対してCORSを許可します
                registry.addMapping("/**")
                        // 許可するオリジン（フロントエンド, データベースのURL）を指定します
                        .allowedOrigins(
                            "http://localhost:5173",
                            "http://127.0.0.1:5173",
                            "http://localhost:3306",
                            "http://127.0.0.1:3306",
                            "https://smart-household-account-book.com")
                        // 許可するHTTPメソッドを指定します
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        // Cookieなどの認証情報を許可する場合はtrueにします
                        .allowCredentials(true);
            }
        };
    }
}