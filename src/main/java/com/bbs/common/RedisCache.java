package com.bbs.common;

import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis 缓存操作封装
 * 提供 String / Hash / List / Set 常用操作
 */
@Component
public class RedisCache {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    // ==================== String 操作 ====================

    /**
     * 设置缓存（无过期时间）
     */
    public void set(String key, Object value) {
        ValueOperations<String, Object> ops = redisTemplate.opsForValue();
        ops.set(key, value);
    }

    /**
     * 设置缓存（带过期时间）
     */
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        ValueOperations<String, Object> ops = redisTemplate.opsForValue();
        ops.set(key, value, timeout, unit);
    }

    /**
     * 获取缓存
     */
    public Object get(String key) {
        ValueOperations<String, Object> ops = redisTemplate.opsForValue();
        return ops.get(key);
    }

    /**
     * 获取缓存并转换为指定类型
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        Object value = get(key);
        if (value == null) {
            return null;
        }
        return (T) value;
    }

    /**
     * 删除缓存
     */
    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    /**
     * 批量删除缓存
     */
    public Long delete(Collection<String> keys) {
        return redisTemplate.delete(keys);
    }

    /**
     * 设置过期时间
     */
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        return redisTemplate.expire(key, timeout, unit);
    }

    /**
     * 判断Key是否存在
     */
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    // ==================== Hash 操作 ====================

    /**
     * Hash - 设置字段值
     */
    public void hSet(String key, String hashKey, Object value) {
        HashOperations<String, String, Object> ops = redisTemplate.opsForHash();
        ops.put(key, hashKey, value);
    }

    /**
     * Hash - 批量设置
     */
    public void hSetAll(String key, Map<String, Object> map) {
        HashOperations<String, String, Object> ops = redisTemplate.opsForHash();
        ops.putAll(key, map);
    }

    /**
     * Hash - 获取字段值
     */
    public Object hGet(String key, String hashKey) {
        HashOperations<String, String, Object> ops = redisTemplate.opsForHash();
        return ops.get(key, hashKey);
    }

    /**
     * Hash - 获取所有字段和值
     */
    public Map<String, Object> hGetAll(String key) {
        HashOperations<String, String, Object> ops = redisTemplate.opsForHash();
        return ops.entries(key);
    }

    /**
     * Hash - 删除字段
     */
    public Long hDelete(String key, Object... hashKeys) {
        HashOperations<String, String, Object> ops = redisTemplate.opsForHash();
        return ops.delete(key, hashKeys);
    }

    /**
     * Hash - 判断字段是否存在
     */
    public Boolean hHasKey(String key, String hashKey) {
        HashOperations<String, String, Object> ops = redisTemplate.opsForHash();
        return ops.hasKey(key, hashKey);
    }

    // ==================== List 操作 ====================

    /**
     * List - 从左侧推入
     */
    public Long lPush(String key, Object value) {
        ListOperations<String, Object> ops = redisTemplate.opsForList();
        return ops.leftPush(key, value);
    }

    /**
     * List - 从右侧推入
     */
    public Long rPush(String key, Object value) {
        ListOperations<String, Object> ops = redisTemplate.opsForList();
        return ops.rightPush(key, value);
    }

    /**
     * List - 获取列表所有元素
     */
    public List<Object> lRange(String key, long start, long end) {
        ListOperations<String, Object> ops = redisTemplate.opsForList();
        return ops.range(key, start, end);
    }

    /**
     * List - 获取列表长度
     */
    public Long lSize(String key) {
        ListOperations<String, Object> ops = redisTemplate.opsForList();
        return ops.size(key);
    }

    // ==================== Set 操作 ====================

    /**
     * Set - 添加元素
     */
    public Long sAdd(String key, Object... values) {
        SetOperations<String, Object> ops = redisTemplate.opsForSet();
        return ops.add(key, values);
    }

    /**
     * Set - 获取所有成员
     */
    public Set<Object> sMembers(String key) {
        SetOperations<String, Object> ops = redisTemplate.opsForSet();
        return ops.members(key);
    }

    /**
     * Set - 判断是否为成员
     */
    public Boolean sIsMember(String key, Object value) {
        SetOperations<String, Object> ops = redisTemplate.opsForSet();
        return ops.isMember(key, value);
    }

    /**
     * Set - 移除元素
     */
    public Long sRemove(String key, Object... values) {
        SetOperations<String, Object> ops = redisTemplate.opsForSet();
        return ops.remove(key, values);
    }

    /**
     * Set - 获取集合大小
     */
    public Long sSize(String key) {
        SetOperations<String, Object> ops = redisTemplate.opsForSet();
        return ops.size(key);
    }
}