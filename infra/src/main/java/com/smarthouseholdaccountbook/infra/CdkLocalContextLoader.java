package com.smarthouseholdaccountbook.infra;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import software.amazon.awscdk.App;

/**
 * Git 管理外の {@code cdk.local.json} を CDK App の context にマージする。
 * <p>
 * CDK CLI の {@code -c} や {@code --context-from-json} に依存せず、
 * {@code cdk deploy / destroy} 実行時に Maven から起動される Java 側で読み込む。
 */
final class CdkLocalContextLoader {

    // フラットな JSON オブジェクト（文字列・真偽値・整数）向けの簡易パーサ
    private static final Pattern ENTRY =
            Pattern.compile("\"([^\"]+)\"\\s*:\\s*(\"([^\"]*)\"|true|false|-?\\d+)");

    private CdkLocalContextLoader() {
    }

    static void apply(final App app) {
        Path local = Path.of("cdk.local.json");
        if (!Files.isRegularFile(local)) {
            return;
        }

        try {
            String json = Files.readString(local);
            Matcher matcher = ENTRY.matcher(json);
            while (matcher.find()) {
                String key = matcher.group(1);
                String stringValue = matcher.group(3);
                if (stringValue != null) {
                    app.getNode().setContext(key, stringValue);
                    continue;
                }

                String raw = matcher.group(2);
                if ("true".equals(raw) || "false".equals(raw)) {
                    app.getNode().setContext(key, Boolean.parseBoolean(raw));
                } else {
                    app.getNode().setContext(key, Integer.parseInt(raw));
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("cdk.local.json の読み込みに失敗しました", e);
        }
    }
}
