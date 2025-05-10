# DefaultApi

All URIs are relative to *http://localhost:8080*

|Method | HTTP request | Description|
|------------- | ------------- | -------------|
|[**apiExpensesGet**](#apiexpensesget) | **GET** /api/expenses | 家計簿データ一覧取得|
|[**apiExpensesIdDelete**](#apiexpensesiddelete) | **DELETE** /api/expenses/{id} | 家計簿データ削除|
|[**apiExpensesPost**](#apiexpensespost) | **POST** /api/expenses | 家計簿データ追加|

# **apiExpensesGet**
> Array<ExpenseDto> apiExpensesGet()


### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

const { status, data } = await apiInstance.apiExpensesGet();
```

### Parameters
This endpoint does not have any parameters.


### Return type

**Array<ExpenseDto>**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 家計簿データ一覧取得成功 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiExpensesIdDelete**
> apiExpensesIdDelete()


### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let id: number; // (default to undefined)

const { status, data } = await apiInstance.apiExpensesIdDelete(
    id
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **id** | [**number**] |  | defaults to undefined|


### Return type

void (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**204** | 家計簿データ削除成功 |  -  |
|**404** | 家計簿データが見つからない |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiExpensesPost**
> ExpenseDto apiExpensesPost(expenseRequestDto)


### Example

```typescript
import {
    DefaultApi,
    Configuration,
    ExpenseRequestDto
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let expenseRequestDto: ExpenseRequestDto; //

const { status, data } = await apiInstance.apiExpensesPost(
    expenseRequestDto
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **expenseRequestDto** | **ExpenseRequestDto**|  | |


### Return type

**ExpenseDto**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**201** | 家計簿データ追加成功 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

