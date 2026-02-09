# API接口调用示例

## 访问统计接口

### 1. 记录用户访问
**POST** `/system/visit/record`
```json
{
  "userId": 123,
  "webId": "website1",
  "pageUrl": "https://example.com/page1",
  "pageTitle": "首页"
}
```

### 2. 心跳检测更新停留时长
**POST** `/system/visit/heartbeat`
```json
{
  "visitId": 456,
  "duration": 30
}
```

### 3. 获取用户今日访问统计
**POST** `/system/visit/today-stats`
```json
{
  "userId": 123,
  "webId": "website1"
}
```

### 4. 获取用户平均停留时间
**POST** `/system/visit/avg-duration`
```json
{
  "userId": 123,
  "webId": "website1",
  "startDate": "2024-01-01 00:00:00",
  "endDate": "2024-01-31 23:59:59"
}
```

### 5. 获取用户月度访问统计
**POST** `/system/visit/monthly-stats`
```json
{
  "userId": 123,
  "webId": "website1"
}
```

### 6. 获取访问趋势数据
**POST** `/system/visit/trend`
```json
{
  "userId": 123,
  "webId": "website1",
  "periodType": "day",
  "startDate": "2024-01-01 00:00:00",
  "endDate": "2024-01-31 23:59:59"
}
```

## 图片管理接口

### 1. 上传文件
**POST** `/system/image/upload`
- Content-Type: multipart/form-data
- 参数: file (文件)

### 2. 下载文件
**POST** `/system/image/download`
```json
{
  "positionCode": "123"
}
```

### 3. 根据位置编码获取图片信息
**POST** `/system/image/position`
```json
{
  "webId": "website1",
  "positionCode": "banner_top"
}
```

### 4. 获取指定网站的图片列表
**POST** `/system/image/list`
```json
{
  "webId": "website1"
}
```

## 前端JavaScript调用示例

```javascript
// 记录访问
async function recordVisit() {
  const response = await fetch('/system/visit/record', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      userId: 123,
      webId: 'website1',
      pageUrl: window.location.href,
      pageTitle: document.title
    })
  });
  return await response.json();
}

// 获取统计信息
async function getTodayStats() {
  const response = await fetch('/system/visit/today-stats', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      userId: 123,
      webId: 'website1'
    })
  });
  return await response.json();
}
```