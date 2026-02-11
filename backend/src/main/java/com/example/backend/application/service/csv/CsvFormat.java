package com.example.backend.application.service.csv;

/**
 * CSV形式の列挙型
 */
public enum CsvFormat {
    /** 三井住友カード 旧形式（2025/12以前） */
    MITSUISUMITOMO_OLD_FORMAT,

    /** 三井住友カード 新形式（2026/1以降） */
    MITSUISUMITOMO_NEW_FORMAT
}
