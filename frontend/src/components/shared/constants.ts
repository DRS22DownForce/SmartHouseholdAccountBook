export const CATEGORIES = ['食費', '日用品', '交通費', '娯楽', 'その他'] as const;

export const FORM_VALIDATION = {
  REQUIRED_FIELDS: ['date', 'category', 'amount', 'description'],
  MIN_AMOUNT: 1,
} as const;