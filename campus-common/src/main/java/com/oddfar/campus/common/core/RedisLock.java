package com.oddfar.campus.common.core;

import com.oddfar.campus.common.utils.SpringUtils;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis分布式锁工具类
 * 用于在多实例部署环境中确保定时任务只执行一次
 * 
 * @author AI Assistant
 */
@Component
public class RedisLock {

    private static final String LOCK_PREFIX = "lock:";
    
    /**
     * 获取RedisTemplate实例
     */
    private RedisTemplate<Object, Object> getRedisTemplate() {
        // 从Spring容器中获取RedisCache实例，再获取其内部的redisTemplate
        RedisCache redisCache = SpringUtils.getBean(RedisCache.class);
        return redisCache.redisTemplate;
    }
    
    /**
     * 获取分布式锁
     * @param lockKey 锁的键名
     * @param requestId 请求标识（建议使用UUID，用于防止误删其他实例的锁）
     * @param expireTime 过期时间（秒）
     * @return 是否获取成功
     */
    public boolean acquireLock(String lockKey, String requestId, int expireTime) {
        // 构建完整的锁键
        String fullKey = LOCK_PREFIX + lockKey;
        
        // 使用Redis的setIfAbsent命令实现分布式锁
        // NX: 只有当key不存在时才设置值
        // PX: 设置过期时间（毫秒）
        return getRedisTemplate().opsForValue().setIfAbsent(fullKey, requestId, expireTime, TimeUnit.SECONDS);
    }
    
    /**
     * 释放分布式锁
     * @param lockKey 锁的键名
     * @param requestId 请求标识（必须与获取锁时的requestId一致）
     * @return 是否释放成功
     */
    public boolean releaseLock(String lockKey, String requestId) {
        // 构建完整的锁键
        String fullKey = LOCK_PREFIX + lockKey;
        
        // 先获取锁的值，验证是否是自己的锁
        Object currentValue = getRedisTemplate().opsForValue().get(fullKey);
        
        // 如果值存在且等于requestId，说明是自己的锁，可以释放
        if (requestId.equals(currentValue)) {
            // 删除锁
            return getRedisTemplate().delete(fullKey);
        }
        
        // 不是自己的锁，不释放
        return false;
    }
    
    /**
     * 检查锁是否存在
     * @param lockKey 锁的键名
     * @return 锁是否存在
     */
    public boolean isLocked(String lockKey) {
        String fullKey = LOCK_PREFIX + lockKey;
        return getRedisTemplate().hasKey(fullKey);
    }
    
    /**
     * 获取锁的过期时间（秒）
     * @param lockKey 锁的键名
     * @return 过期时间，如果锁不存在则返回-1
     */
    public long getLockExpire(String lockKey) {
        String fullKey = LOCK_PREFIX + lockKey;
        return getRedisTemplate().getExpire(fullKey, TimeUnit.SECONDS);
    }
}