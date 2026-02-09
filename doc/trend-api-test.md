# 访问趋势数据API测试说明

## API接口
**POST** `/system/visit/trend`

## 请求参数
```json
{
  "userId": 123,
  "webId": "website1",
  "periodType": "day",
  "startDate": "2024-01-01 00:00:00",
  "endDate": "2024-01-31 23:59:59"
}
```

## 支持的时间维度类型
- `hour`: 按小时统计（返回24小时数据）
- `day`: 按天统计（返回日期范围内每天的数据）
- `month`: 按月统计（返回月份范围内每月的数据）

## 返回数据格式
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": [
    {
      "timeLabel": "00:00",
      "visitCount": 15,
      "uniqueUsers": 8,
      "totalDuration": 3600,
      "avgDuration": 240.0
    },
    {
      "timeLabel": "01:00",
      "visitCount": 12,
      "uniqueUsers": 6,
      "totalDuration": 2880,
      "avgDuration": 240.0
    }
  ]
}
```

## 字段说明
- `timeLabel`: 时间标签（小时格式如"00:00"，日期格式如"2024-01-01"，月份格式如"2024-01"）
- `visitCount`: 访问次数
- `uniqueUsers`: 独立用户数
- `totalDuration`: 总停留时长（秒）
- `avgDuration`: 平均停留时长（秒）

## 使用示例

### 按小时统计
```javascript
fetch('/system/visit/trend', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    userId: 123,
    webId: 'website1',
    periodType: 'hour',
    startDate: '2024-01-01 00:00:00',
    endDate: '2024-01-01 23:59:59'
  })
})
.then(response => response.json())
.then(data => {
  console.log('小时趋势数据:', data.data);
});
```

### 按天统计
```javascript
fetch('/system/visit/trend', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    userId: 123,
    webId: 'website1',
    periodType: 'day',
    startDate: '2024-01-01 00:00:00',
    endDate: '2024-01-31 23:59:59'
  })
})
.then(response => response.json())
.then(data => {
  console.log('日趋势数据:', data.data);
});
```

### 按月统计
```javascript
fetch('/system/visit/trend', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    userId: 123,
    webId: 'website1',
    periodType: 'month',
    startDate: '2023-01-01 00:00:00',
    endDate: '2023-12-31 23:59:59'
  })
})
.then(response => response.json())
.then(data => {
  console.log('月趋势数据:', data.data);
});
```

## 注意事项
1. 日期格式必须为 "yyyy-MM-dd HH:mm:ss"
2. endDate必须大于等于startDate
3. 对于单用户查询，uniqueUsers通常为1
4. 超过5分钟的访问时长不会被计入统计