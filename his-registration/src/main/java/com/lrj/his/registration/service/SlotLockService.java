package com.lrj.his.registration.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 号源原子锁。用 Redis Lua 脚本保证"检查剩余 + 扣减"在单线程内原子完成,
 * 杜绝高并发超挂(oversell)。Redis 库存懒加载:首次抢号时按 DB 总数初始化。
 */
@Service
public class SlotLockService {

    private static final String KEY_PREFIX = "reg:slots:";

    /** EXISTS 则不动;否则用 ARGV 初始化。剩余>0 则 DECR 返回 1,否则返回 0。 */
    private static final DefaultRedisScript<Long> ACQUIRE = new DefaultRedisScript<>("""
            if redis.call('EXISTS', KEYS[1]) == 0 then
              redis.call('SET', KEYS[1], ARGV[1])
            end
            local n = tonumber(redis.call('GET', KEYS[1]))
            if n <= 0 then
              return 0
            end
            redis.call('DECR', KEYS[1])
            return 1
            """, Long.class);

    private final StringRedisTemplate redis;

    public SlotLockService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    /**
     * 尝试占用一个号源。
     *
     * @param scheduleId   排班ID
     * @param initialStock DB 权威总号源(首次懒加载用)
     * @return true=占用成功
     */
    public boolean tryAcquire(Long scheduleId, int initialStock) {
        Long r = redis.execute(ACQUIRE, List.of(key(scheduleId)), String.valueOf(initialStock));
        return r != null && r == 1L;
    }

    /** 退号时归还库存。 */
    public void release(Long scheduleId) {
        redis.opsForValue().increment(key(scheduleId));
    }

    private String key(Long scheduleId) {
        return KEY_PREFIX + scheduleId;
    }
}
