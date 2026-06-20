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

        // CDK 標準の context（cdk.json / cdk.context.json）だけを読みます。
        String region = requiredContextString(app, "awsRegion");

        String account = System.getenv("CDK_DEFAULT_ACCOUNT");
        if (account == null || account.isBlank()) {
            throw new IllegalStateException(
                    "CDK_DEFAULT_ACCOUNT is required. Run this app through the CDK CLI with AWS credentials.");
        }
        Environment env = Environment.builder().account(account.trim()).region(region).build();

        new SmartHouseholdStack(app, "SmartHouseholdStack", StackProps.builder()
                .env(env)
                .description("Smart Household Account Book - EC2 + Docker Compose (single host)")
                .build());

        app.synth();
    }

    static String requiredContextString(final Construct scope, final String key) {
        Object value = scope.getNode().tryGetContext(key);
        if (value == null) {
            throw new IllegalArgumentException(
                    "CDK context '" + key + "' is required. Set it in cdk.json or cdk.context.json.");
        }
        String text = value.toString().trim();
        if (text.isEmpty()) {
            throw new IllegalArgumentException(
                    "CDK context '" + key + "' must not be blank. Set it in cdk.json or cdk.context.json.");
        }
        return text;
    }

    static String optionalContextString(final Construct scope, final String key, final String defaultValue) {
        Object value = scope.getNode().tryGetContext(key);
        if (value == null) {
            return defaultValue;
        }
        String text = value.toString().trim();
        return text.isEmpty() ? defaultValue : text;
    }

    static boolean optionalContextBoolean(final Construct scope, final String key, final boolean defaultValue) {
        Object value = scope.getNode().tryGetContext(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Boolean bool) {
            return bool;
        }
        String text = value.toString().trim();
        if ("true".equalsIgnoreCase(text)) {
            return true;
        }
        if ("false".equalsIgnoreCase(text)) {
            return false;
        }
        throw new IllegalArgumentException(
                "CDK context '" + key + "' must be true or false when specified.");
    }

    static int optionalContextInt(final Construct scope, final String key, final int defaultValue) {
        Object value = scope.getNode().tryGetContext(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(value.toString().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "CDK context '" + key + "' must be an integer when specified.", e);
        }
    }
}
