package priv.penuel.simple.lock.zookeeper;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import priv.penuel.simple.lock.DistributedLock;
import priv.penuel.simple.lock.LockHolder;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * @author: penuel
 * @date: 2020-07-08 14:24
 * @desc: TODO
 */
@Component
public class ZKDistributedLock implements DistributedLock {


    @Value("${curator.host}")
    private String host;
    private CuratorFramework client;
    private static final String PATH = "/curator/lock/";

    @Override
    public boolean tryLock(Duration timeout) throws Exception {
        InterProcessMutex mutex = new InterProcessMutex(client, PATH + LockHolder.get().getResource());
        return mutex.acquire(timeout.getNano(), TimeUnit.NANOSECONDS);
    }


    @Override
    public boolean unlock() throws Exception {
        InterProcessMutex mutex = new InterProcessMutex(client, PATH + LockHolder.get().getResource());
        mutex.release();
        return true;
    }

    @PostConstruct
    public void init() {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        client = CuratorFrameworkFactory.newClient(host, retryPolicy);
        client.start();
    }

    @PreDestroy
    public void destroy() {
        if (null != client)
            client.close();
    }
}
