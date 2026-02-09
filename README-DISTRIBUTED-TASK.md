# Spring分布式定时任务实现方案

## 功能说明

本方案提供了在Spring Boot项目中实现分布式定时任务的完整解决方案，确保在多实例部署环境下，相同的定时任务只会被执行一次，避免任务重复执行带来的数据一致性问题。

## 实现原理

本方案基于Redis分布式锁来实现定时任务的互斥执行：

1. 每个定时任务在执行前，先尝试获取Redis分布式锁
2. 只有成功获取锁的实例才能执行任务
3. 任务执行完成后，释放锁供其他实例使用
4. 设置合理的锁过期时间，防止因实例宕机导致锁无法释放

## 文件结构

```
campus-common/src/main/java/com/oddfar/campus/common/core/RedisLock.java       # Redis分布式锁工具类
campus-common/src/main/java/com/oddfar/campus/common/config/SchedulingConfig.java  # 定时任务配置类
campus-modular/src/main/java/com/oddfar/campus/business/task/DistributedScheduledTask.java  # 定时任务示例类
```

## 使用方法

### 1. 添加依赖

确保项目中已添加Redis相关依赖（项目中已有，无需额外添加）：

```xml
<!-- redis 缓存操作 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
<!-- spring2.X集成redis所需common-pool2-->
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-pool2</artifactId>
</dependency>
```

### 2. 配置Redis连接

在`application.yml`或`application.properties`中配置Redis连接信息（项目中已有配置，无需修改）：

```yaml
spring:
  redis:
    host: localhost
    port: 6379
    database: 0
    timeout: 1800000
    password:
    lettuce:
      pool:
        max-active: 20
        max-wait: -1
        max-idle: 5
        min-idle: 0
```

### 3. 创建定时任务类

创建一个定时任务类，使用`@Component`注解将其注册为Spring Bean，并使用`@Scheduled`注解定义任务执行时间：

```java
package com.oddfar.campus.business.task;

import com.oddfar.campus.common.core.RedisLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class YourScheduledTask {

    private static final Logger logger = LoggerFactory.getLogger(YourScheduledTask.class);
    
    @Autowired
    private RedisLock redisLock;
    
    // 锁的过期时间（秒），建议设置为任务执行时间的2-3倍
    private static final int LOCK_EXPIRE_TIME = 30;
    
    @Scheduled(cron = "0 0/1 * * * ?")  // 每分钟执行一次
    public void executeTaskWithLock() {
        // 生成唯一的请求ID，用于标识当前实例
        String requestId = UUID.randomUUID().toString();
        // 定义锁的键名，建议包含任务名称以区分不同任务
        String lockKey = "scheduled:task:your_task_name";
        
        boolean locked = false;
        try {
            // 尝试获取分布式锁
            locked = redisLock.acquireLock(lockKey, requestId, LOCK_EXPIRE_TIME);
            
            if (locked) {
                logger.info("获取分布式锁成功，开始执行定时任务");
                
                // 执行实际的业务逻辑
                doYourBusinessLogic();
                
                logger.info("定时任务执行完成");
            } else {
                // 未获取到锁，说明其他实例正在执行此任务
                logger.info("未获取到分布式锁，任务已被其他实例执行");
            }
        } catch (Exception e) {
            logger.error("定时任务执行异常: {}", e.getMessage(), e);
        } finally {
            // 无论执行成功与否，都要释放锁
            if (locked) {
                redisLock.releaseLock(lockKey, requestId);
            }
        }
    }
    
    /**
     * 实际的业务逻辑
     */
    private void doYourBusinessLogic() {
        // 你的业务逻辑代码
    }
}
```

## 注意事项

1. **锁的过期时间设置**：
   - 锁的过期时间应设置为任务执行时间的2-3倍，确保任务有足够的时间完成
   - 避免设置过长导致锁长时间不释放，也避免设置过短导致任务未完成锁就过期

2. **异常处理**：
   - 务必在`finally`块中释放锁，避免因异常导致锁无法释放
   - 使用`requestId`确保只释放自己创建的锁，避免误删其他实例的锁

3. **Redis可用性**：
   - 确保Redis服务高可用，可考虑使用Redis集群或哨兵模式
   - 监控Redis连接状态，及时处理Redis连接异常

4. **任务幂等性**：
   - 尽管使用了分布式锁，仍建议确保任务的幂等性，以防极端情况下锁失效导致任务重复执行

5. **并发控制**：
   - 定时任务的线程池大小应根据任务数量和系统资源合理配置
   - 避免任务执行时间过长阻塞线程池

## 常见问题

### 1. 锁过期但任务还在执行怎么办？

**解决方案**：
- 合理估计任务执行时间，设置稍长的过期时间
- 实现锁续期机制（看门狗机制），在任务执行过程中定期延长锁的过期时间
- 确保任务幂等性，即使重复执行也不会产生副作用

### 2. Redis连接失败如何处理？

**解决方案**：
- 添加Redis连接重试机制
- 实现降级策略，如在Redis不可用时记录日志并告警，等待Redis恢复
- 考虑使用Redis哨兵或集群提高可用性

### 3. 如何监控定时任务的执行情况？

**解决方案**：
- 完善日志记录，记录任务开始、结束、成功、失败等关键节点
- 实现任务执行状态的监控和告警机制
- 考虑使用分布式任务调度框架如Quartz、XXL-JOB等，提供更完善的监控功能

## 性能优化建议

1. **合理设置线程池大小**，避免线程过多或过少
2. **优化任务执行效率**，减少任务执行时间
3. **使用批处理**代替逐条处理，减少数据库交互次数
4. **考虑使用异步处理**非关键路径的业务逻辑
5. **定期清理Redis中过期的锁**，避免占用过多内存

---

本实现方案已在项目中集成，可直接参考`DistributedScheduledTask`类的示例代码进行使用。