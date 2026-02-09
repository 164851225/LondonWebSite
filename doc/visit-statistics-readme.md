# 用户访问统计系统使用说明

## 系统功能概述

本系统实现了完整的用户访问统计和分析功能，包括：

### 核心功能
1. **用户访问记录** - 记录用户每次页面访问
2. **心跳检测** - 每秒检测用户活跃度，超5分钟停止记录
3. **停留时长统计** - 精确统计用户在页面的停留时间
4. **多维度统计** - 支持今日、月度、趋势等多种统计维度
5. **数据可视化** - 提供访问趋势折线图数据

## 数据库表结构

### 1. user_visit_record (用户访问记录表)
存储用户的每次页面访问详情

### 2. user_daily_stats (用户每日统计表)  
存储用户每日的访问汇总数据

### 3. web_visit_summary (网站访问汇总表)
存储网站整体的访问统计数据

## API接口说明

### 访问记录接口
- **POST** `/system/visit/record` - 记录用户访问
- **POST** `/system/visit/heartbeat/{visitId}` - 心跳检测更新时长

### 统计查询接口
- **GET** `/system/visit/today-stats` - 获取今日访问统计
- **GET** `/system/visit/avg-duration` - 获取平均停留时间
- **GET** `/system/visit/monthly-stats` - 获取月度访问统计
- **GET** `/system/visit/trend` - 获取访问趋势数据

## 前端集成使用

### 1. 引入JavaScript SDK
```html
<script src="/js/visit-tracker.js"></script>
```

### 2. 初始化访问追踪
```javascript
const tracker = new VisitTracker({
    userId: 123,           // 用户ID
    webId: 'website1',     // 网站ID
    apiUrl: '/system/visit' // API基础路径
});
```

### 3. 获取统计数据
```javascript
// 获取今日统计
const todayStats = await tracker.getTodayStats();

// 获取月度统计  
const monthlyStats = await tracker.getMonthlyStats();
```

## 部署配置

### 1. 数据库初始化
执行 `doc/sql/visit_statistics_tables.sql` 创建相关数据表

### 2. 确保依赖
- eu.bitwalker.UserAgentUtils (用于解析用户代理信息)
- 确保相关Mapper XML文件正确加载

### 3. 启动应用
系统会自动注册相关服务和控制器

## 功能特点

✅ **精准统计** - 每秒心跳检测，精确到秒的停留时长统计
✅ **智能过滤** - 超过5分钟的访问不计入统计
✅ **多维分析** - 支持小时、天、月等多时间维度分析
✅ **实时监控** - 提供实时的心跳检测和数据更新
✅ **灵活配置** - 支持多网站、多用户独立统计
✅ **数据安全** - 完整的异常处理和数据保护机制

## 注意事项

1. 确保服务器时间同步准确
2. 建议定期清理历史访问记录数据
3. 前端需正确引入visit-tracker.js文件
4. 用户ID和网站ID需要在业务中统一管理