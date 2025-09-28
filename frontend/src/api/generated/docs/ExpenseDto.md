# ExpenseDto


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **number** | 家計簿データの一意ID | [optional] [default to undefined]
**date** | **string** | 支出日 | [optional] [default to undefined]
**category** | **string** | 支出カテゴリ | [optional] [default to undefined]
**amount** | **number** | 支出金額 | [optional] [default to undefined]
**description** | **string** | 支出の詳細説明 | [optional] [default to undefined]

## Example

```typescript
import { ExpenseDto } from './api';

const instance: ExpenseDto = {
    id,
    date,
    category,
    amount,
    description,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
