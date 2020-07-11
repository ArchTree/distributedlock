package priv.penuel.simple.lock;

import java.time.Duration;

public interface DistributedLock {

    boolean tryLock(Duration timeout) throws Exception;

    boolean unlock() throws Exception;
}
