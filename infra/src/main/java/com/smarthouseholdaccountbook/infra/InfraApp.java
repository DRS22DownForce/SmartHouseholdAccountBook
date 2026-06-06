package com.smarthouseholdaccountbook.infra;

import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;
import software.constructs.Construct;

/**
 * CDK アプリケーションのエントリポイント。
 * <p>
 * {@code cdk deploy} 実行時に Maven がこの main を起動し、
 * CloudFormation テンプレート（インフラの設計図）を生成します。
 */
public final class InfraApp {

    private InfraApp() {
    }

    public static void main(final String[] args) {
        App app = new App();

        // cdk.json の context からリージョンを読み取る（未指定なら CLI のデフォルト）
        String region = (String) app.getNode().tryGetContext("awsRegion");
        if (region == null || region.isBlank()) {
            region = System.getenv("CDK_DEFAULT_REGION");
        }
        if (region == null || region.isBlank()) {
            region = "ap-northeast-1";
        }

        String account = System.getenv("CDK_DEFAULT_ACCOUNT");
        Environment env = account != null && !account.isBlank()
                ? Environment.builder().account(account).region(region).build()
                : null;

        new SmartHouseholdStack(app, "SmartHouseholdStack", StackProps.builder()
                .env(env)
                .description("Smart Household Account Book - EC2 + Docker Compose (single host)")
                .build());

        app.synth();
    }

    static String contextString(final Construct scope, final String key, final String defaultValue) {
        Object value = scope.getNode().tryGetContext(key);
        if (value == null) {
            return defaultValue;
        }
        String text = value.toString().trim();
        return text.isEmpty() ? defaultValue : text;
    }

    static boolean contextBoolean(final Construct scope, final String key, final boolean defaultValue) {
        Object value = scope.getNode().tryGetContext(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Boolean bool) {
            return bool;
        }
        return Boolean.parseBoolean(value.toString());
    }

    static int contextInt(final Construct scope, final String key, final int defaultValue) {
        Object value = scope.getNode().tryGetContext(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        return Integer.parseInt(value.toString());
    }
}
