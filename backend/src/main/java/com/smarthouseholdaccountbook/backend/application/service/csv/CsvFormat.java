package com.smarthouseholdaccountbook.backend.application.service.csv;

/**
 * CSV形式の列挙型
 */
public enum CsvFormat {
    /** 三井住友カード 確定月の明細CSV */
    MITSUISUMITOMO_OLD_FORMAT,

    /** 三井住友カード 未確定月の明細CSV */
    MITSUISUMITOMO_NEW_FORMAT
}
