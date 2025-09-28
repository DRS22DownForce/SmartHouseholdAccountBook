# ExpenseRequestDto


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**date** | **string** | 支出日 | [default to undefined]
**category** | **string** | 支出カテゴリ | [default to undefined]
**amount** | **number** | 支出金額 | [default to undefined]
**description** | **string** | 支出の詳細説明 | [optional] [default to undefined]

## Example

```typescript
import { ExpenseRequestDto } from './api';

const instance: ExpenseRequestDto = {
    date,
    category,
    amount,
    description,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
