package priv.penuel.simple.lock.etcd;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.Lease;
import io.etcd.jetcd.Lock;
import io.etcd.jetcd.options.LeaseOption;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import priv.penuel.simple.lock.DistributedLock;
import priv.penuel.simple.lock.LockHolder;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * @author: penuel
 * @date: 2020-07-08 17:17
 * @desc: TODO
 */
@Component
public class EtcdDistributedLock implements DistributedLock {

    @Value("${etcd.endpoint}")
    private String endpoint;

    private Client client; // etcd客户端
    private Lock lockClient; // etcd分布式锁客户端
    private Lease leaseClient; // etcd租约客户端
    private long TTL = 60;//second


    @Override
    public boolean tryLock(Duration timeout) throws Exception {
        priv.penuel.simple.lock.Lock lock = LockHolder.get();
        long leaseId = Long.valueOf(lock.getNode());
        if (leaseId == 0) {
            leaseId = leaseClient.grant(TTL).get().getID();
            lock.setNode(String.valueOf(leaseId));
            LockHolder.set(lock);
        }
        //在指定租约上获取lock
        lockClient.lock(ByteSequence.from(LockHolder.get().getResource().getBytes()), leaseId).get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        //如果该租约已经过期，则获取失败
        long ttl = leaseClient.timeToLive(leaseId, LeaseOption.DEFAULT).get(1, TimeUnit.SECONDS).getTTl();
        if (ttl > 0) {
            return true;
        } else {
            return false;
        }
    }


    @Override
    public boolean unlock() throws Exception {
        long leaseId = Long.valueOf(LockHolder.get().getNode());
        //试图释放锁
        lockClient.unlock(ByteSequence.from(LockHolder.get().getResource().getBytes())).get();
        leaseClient.revoke(leaseId).get(1, TimeUnit.SECONDS);

        return false;
    }


    @PostConstruct
    public void init() {
        this.client = Client.builder().endpoints(endpoint).build();
        this.lockClient = client.getLockClient();
        this.leaseClient = client.getLeaseClient();
    }


}
