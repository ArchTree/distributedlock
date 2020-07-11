package priv.penuel.simple.lock.redis;

import org.springframework.stereotype.Component;
import priv.penuel.simple.lock.DistributedLock;

import java.time.Duration;

/**
 * @author: penuel
 * @date: 2020-07-06 11:55
 * @desc: TODO
 */
@Component
public class RedLock implements DistributedLock {
    @Override
    public boolean tryLock(Duration timeout) {
        return false;
    }


    @Override
    public boolean unlock() {
        return false;
    }
}
