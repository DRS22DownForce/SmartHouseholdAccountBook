package com.smarthouseholdaccountbook.backend.application.service.csv;

/**
 * CSV形式の列挙型。
 *
 * 三井住友カードの明細CSVは、Vpassの「確定月」と「未確定月」で列構成が異なる。
 * 新旧フォーマットではなく、その2種類を表す。
 */
public enum CsvFormat {
    /** 三井住友カード 確定月の明細CSV（ご利用日,ご利用店名,ご利用金額,...） */
    MITSUISUMITOMO_CONFIRMED_MONTH,

    /** 三井住友カード 未確定月の明細CSV（ご利用日,ご利用店名,カード,支払区分,...） */
    MITSUISUMITOMO_UNCONFIRMED_MONTH
}
