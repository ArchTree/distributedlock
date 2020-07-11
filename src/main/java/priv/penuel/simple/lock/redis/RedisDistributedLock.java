package priv.penuel.simple.lock.redis;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import priv.penuel.simple.lock.DistributedLock;
import priv.penuel.simple.lock.Lock;
import priv.penuel.simple.lock.LockHolder;

import javax.annotation.Resource;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.concurrent.locks.LockSupport;

/**
 * @author: penuel
 * @date: 2020-07-06 10:16
 * @desc: TODO
 */
@Component
public class RedisDistributedLock implements DistributedLock {

    @Resource
    private RedisTemplate redisTemplate;
    private long TTL = 60;//second

    @Override
    public boolean tryLock(Duration timeout) {
        long start = Instant.now().toEpochMilli();
        Lock lock = LockHolder.get();
        while (true) {
            long end = Instant.now().toEpochMilli();
            if (end - start > timeout.toMillis()) {
                return false;
            }
            Boolean save = redisTemplate.opsForValue().setIfAbsent(lock.getResource(), lock.getNode(), Duration.ofSeconds(TTL));
            if (null != save && save) {
                return true;
            } else {
                LockSupport.parkNanos(500 * 1000);
            }
        }
    }


    /*
   #unlock.lua
   if redis.call('get', KEYS[1]) == ARGV[1] then
       redis.call('pexpire', KEYS[1], ARGV[2])
       return 1
   end
   return 0
    */
    @Override
    public boolean unlock() {
        Lock lock = LockHolder.get();
        DefaultRedisScript<Boolean> holdScript = new DefaultRedisScript<>();
        holdScript.setLocation(new ClassPathResource("lua/unlock.lua"));
        holdScript.setResultType(Boolean.class);
        Boolean result = (Boolean) redisTemplate.execute(holdScript, Collections.singletonList(lock.getResource()), lock.getNode(), 0l);
        return result.booleanValue();
    }


    /*
    redission.lock==>
    1. 使用hash对每个锁key(资源节点信息)进行赋值value(锁的次数)。实现可重入的加锁方式(对value进行加1操作)
    2. 如果加锁失败，判断是否超时，如果超时则返回false。
    3. 如果加锁失败，没有超时，那么需要在redisson_lock__channel+lockName的channel上进行订阅，用于订阅解锁消息，然后一直阻塞直到超时，或者有解锁消息。
    4. 重试步骤1，2，3，直到最后获取到锁，或者某一步获取锁超时。


        if (redis.call('exists', KEYS[1]) == 0) then
            redis.call('hset', KEYS[1], ARGV[2], 1);
            redis.call('pexpire', KEYS[1], ARGV[1]);
            return nil;
        end;
        if (redis.call('hexists', KEYS[1], ARGV[2]) == 1) then
            redis.call('hincrby', KEYS[1], ARGV[2], 1);
            redis.call('pexpire', KEYS[1], ARGV[1]);
            return nil;
        end;
        return redis.call('pttl', KEYS[1]);
     */

    /*
    redission.unlock==>
    1. 通过lua脚本进行解锁，如果是可重入锁，只是减1。如果是非加锁线程解锁，那么解锁失败。
    2. 解锁成功需要在redisson_lock__channel+lockName的channel发布解锁消息，以便等待该锁的线程进行加锁

        if (redis.call('exists', KEYS[1]) == 0) then
            redis.call('publish', KEYS[2], ARGV[1]);
            return 1;
        end;
        if (redis.call('hexists', KEYS[1], ARGV[3]) == 0) then
            return nil;
        end;
        local counter = redis.call('hincrby', KEYS[1], ARGV[3], -1);
        if (counter > 0) then
            redis.call('pexpire', KEYS[1], ARGV[2]);
            return 0;
        else
            redis.call('del', KEYS[1]);
            redis.call('publish', KEYS[2], ARGV[1]);
            return 1;
        end;
        return nil;
     */
}
