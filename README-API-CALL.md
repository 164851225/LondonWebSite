# API接口调用工具使用说明

本文档详细说明如何使用项目中创建的HTTP接口调用工具类和API服务，包括登录接口和灯具数据接口的调用方法。

## 1. 文件结构

```
campus-common/src/main/java/com/oddfar/campus/common/
├── model/api/                    # API请求和响应实体类
│   ├── LoginRequest.java         # 登录请求实体类
│   ├── LoginResponse.java        # 登录响应实体类
│   ├── LoginResult.java          # 登录结果实体类
│   ├── MeterDataRequest.java     # 灯具数据请求实体类
│   ├── MeterDataResponse.java    # 灯具数据响应实体类
│   └── MeterData.java            # 灯具数据项实体类
├── service/api/                  # API服务接口
│   └── ApiService.java           # API服务接口定义
├── service/api/impl/             # API服务实现类
│   └── ApiServiceImpl.java       # API服务实现
└── utils/http/                   # HTTP工具类
    └── HttpUtils.java            # 通用HTTP请求工具类
```

## 2. 组件说明

### 2.1 通用HTTP请求工具类 (HttpUtils)

`HttpUtils` 是一个封装了各种HTTP请求方法的工具类，支持普通GET/POST请求、JSON格式请求、自定义请求头和带认证信息的请求。

主要方法：
- `sendGet(String url)`: 发送GET请求
- `sendPost(String url, String param)`: 发送POST请求（表单格式）
- `sendPostJson(String url, Object requestBody)`: 发送POST请求（JSON格式）
- `sendPostWithHeaders(String url, Object requestBody, Map<String, String> headers)`: 发送带自定义请求头的POST请求
- `sendPostWithToken(String url, Object requestBody, String token)`: 发送带认证token的POST请求
- `parseJson(String json, Class<T> clazz)`: 解析JSON字符串为对象
- `parseJson(String json, TypeReference<T> typeReference)`: 解析JSON字符串为复杂对象（如List、Map等）

### 2.2 API服务接口及实现 (ApiService/ApiServiceImpl)

`ApiService` 定义了与外部API交互的方法接口，`ApiServiceImpl` 提供了具体实现。

主要功能：
- 用户登录并获取访问令牌
- 使用访问令牌获取灯具数据
- 管理访问令牌的缓存
- 清除和刷新访问令牌

### 2.3 实体类

#### 登录相关
- `LoginRequest`: 封装登录请求参数（用户名、密码等）
- `LoginResponse`: 封装登录接口返回的完整响应
- `LoginResult`: 封装登录成功后返回的token信息

#### 灯具数据相关
- `MeterDataRequest`: 封装获取灯具数据的请求参数
- `MeterDataResponse`: 封装灯具数据接口返回的完整响应
- `MeterData`: 封装灯具数据接口返回的单个数据条目

## 3. 使用示例

### 3.1 基本使用流程

```java
// 1. 注入API服务
@Autowired
private ApiService apiService;

// 2. 登录获取访问令牌
LoginResult loginResult = apiService.login("聂平聂平", "e10adc3949ba59abbe56e057f20f883e");
String accessToken = loginResult.getAccessToken();
String refreshToken = loginResult.getRefreshToken();

// 3. 准备灯具数据请求参数
MeterDataRequest meterDataRequest = new MeterDataRequest();
meterDataRequest.setParkId("743192761155654");  // 六楼
meterDataRequest.setMeterId("");
meterDataRequest.setChartTime("2025-11-21");
meterDataRequest.setTimeQueryEnum(0);

// 4. 获取灯具数据（传入用户名，服务会自动从缓存中获取对应的访问令牌）
MeterDataResponse meterDataResponse = apiService.getMeterData(meterDataRequest, "聂平聂平");

// 5. 处理返回的灯具数据
List<MeterData> meterDataList = meterDataResponse.getResult();
for (MeterData data : meterDataList) {
    System.out.println("日期: " + data.getChartTime());
    System.out.println("当前能耗: " + data.getCurrentEnergy());
    System.out.println("节约能耗: " + data.getSavingEnergy());
    System.out.println("------------------------");
}

// 6. 登出时清除访问令牌
apiService.clearAccessToken("聂平聂平");
```

### 3.2 直接使用HttpUtils（不通过ApiService）

```java
// 1. 登录获取访问令牌
LoginRequest loginRequest = new LoginRequest();
loginRequest.setAccount("聂平聂平");
loginRequest.setPassword("e10adc3949ba59abbe56e057f20f883e");

String loginUrl = "http://124.70.131.219:8083/adminApi8/api/sysAuth/loginByLiot";
String loginResponseJson = HttpUtils.sendPostJson(loginUrl, loginRequest);

// 解析登录响应
LoginResponse loginResponse = HttpUtils.parseJson(loginResponseJson, LoginResponse.class);
String accessToken = loginResponse.getResult().getAccessToken();

// 2. 使用访问令牌获取灯具数据
MeterDataRequest meterDataRequest = new MeterDataRequest();
meterDataRequest.setParkId("743192761155654");
meterDataRequest.setChartTime("2025-11-21");

// 设置认证头
Map<String, String> headers = new HashMap<>();
headers.put("Authorization", "Bearer " + accessToken);

String meterDataUrl = "http://124.70.131.219:8083/adminApi8/api/dataEnergy/getMeterDataCharts";
String meterDataResponseJson = HttpUtils.sendPostWithHeaders(meterDataUrl, meterDataRequest, headers);

// 解析灯具数据响应
MeterDataResponse meterDataResponse = HttpUtils.parseJson(meterDataResponseJson, MeterDataResponse.class);
// 处理数据...
```

## 4. 异常处理

在使用API服务时，可能会遇到以下异常情况：

```java
try {
    // API调用代码
    MeterDataResponse response = apiService.getMeterData(request, "聂平聂平");
    // 处理响应
} catch (RuntimeException e) {
    // 处理运行时异常
    log.error("API调用失败: {}", e.getMessage(), e);
    // 可以根据具体错误信息进行重试、通知等操作
}
```

常见异常类型：
- 访问令牌过期或无效
- 网络连接问题
- API返回错误状态码
- 请求参数错误

## 5. 注意事项

1. **访问令牌管理**
   - 访问令牌有过期时间，需要在过期前进行刷新
   - 目前刷新令牌功能尚未完全实现，实际应用中应调用专门的刷新令牌接口

2. **线程安全**
   - 访问令牌缓存在`ConcurrentHashMap`中，保证了多线程环境下的安全性

3. **日志记录**
   - 所有API调用都记录了详细的日志，便于问题排查

4. **密码安全**
   - 示例中的密码是MD5加密后的字符串，实际应用中应注意密码的安全存储和传输

5. **URL配置**
   - API的基础URL硬编码在服务实现类中，在实际应用中可以考虑将其配置到配置文件中

## 6. 扩展建议

1. **增加重试机制**
   - 为API调用添加重试机制，特别是在网络不稳定的情况下

2. **完善令牌管理**
   - 实现完整的令牌刷新功能
   - 添加令牌过期时间管理

3. **添加断路器模式**
   - 集成Hystrix等组件，实现服务熔断和降级

4. **配置外部化**
   - 将API URL、超时时间等配置移至配置文件

5. **添加单元测试**
   - 为各个组件编写单元测试，提高代码质量

---

通过以上文档，您应该能够了解如何使用我们提供的API调用工具类和服务来实现登录和获取灯具数据的功能。如果有任何问题或需要进一步的扩展，可以参考代码中的详细注释或联系开发人员。