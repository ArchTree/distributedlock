package priv.penuel.simple.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import priv.penuel.simple.lock.LockHolder;
import priv.penuel.simple.lock.etcd.EtcdDistributedLock;
import priv.penuel.simple.lock.mysql.MysqlDistributedLock;
import priv.penuel.simple.lock.redis.RedisDistributedLock;
import priv.penuel.simple.lock.zookeeper.ZKDistributedLock;

import javax.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * @author: penuel
 * @date: 2020-07-09 11:39
 * @desc: TODO
 */
@RestController
public class LockController {

    @Resource
    private EtcdDistributedLock etcdDistributedLock;
    @Resource
    private MysqlDistributedLock mysqlDistributedLock;
    @Resource
    private RedisDistributedLock redisDistributedLock;
    @Resource
    private ZKDistributedLock zkDistributedLock;

    @RequestMapping("/")
    public String index() {
        return "success-" + LocalDateTime.now();
    }

    /**
     * zookeeper 分布式锁
     *
     * @param resource
     * @param timeout
     * @param unlock
     * @return
     */
    @RequestMapping("zookeeper")
    public String zookeeperLock(
            @RequestParam("resource") String resource,
            @RequestParam(value = "timeout", defaultValue = "3") int timeout,
            @RequestParam(value = "unlock", defaultValue = "false") boolean unlock) {
        try {
            if (!unlock) {
                LockHolder.get().setResource(resource);
                boolean lock = zkDistributedLock.tryLock(Duration.ofSeconds(timeout));
                return "zookeeper lock-" + lock + "-" + LockHolder.get();
            } else {
                boolean unlock1 = zkDistributedLock.unlock();
                return "zookeeper unlock-" + unlock1 + "-" + LockHolder.get();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "zookeeper OK";
    }

    /**
     * redis 分布式锁
     *
     * @param resource
     * @param node     可重入锁判断
     * @param timeout
     * @param unlock
     * @return
     */
    @RequestMapping("redis")
    public String redisLock(
            @RequestParam("resource") String resource,
            @RequestParam("node") String node,
            @RequestParam(value = "timeout", defaultValue = "3") int timeout,
            @RequestParam(value = "unlock", defaultValue = "false") boolean unlock) {
        try {
            if (!unlock) {
                LockHolder.get().setResource(resource);
                LockHolder.get().setNode(node);
                boolean lock = redisDistributedLock.tryLock(Duration.ofSeconds(timeout));
                return "redis lock-" + lock + "-" + LockHolder.get();
            } else {
                boolean unlock1 = redisDistributedLock.unlock();
                return "redis unlock-" + unlock1 + "-" + LockHolder.get();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "OK";
    }

    /**
     * mysql 分布式锁
     *
     * @param resource
     * @param node     可重入锁判断
     * @param timeout
     * @param unlock
     * @return
     */
    @RequestMapping("mysql")
    public String mysqlLock(
            @RequestParam("resource") String resource,
            @RequestParam(value = "node", defaultValue = "0") String node,
            @RequestParam(value = "timeout", defaultValue = "3") int timeout,
            @RequestParam(value = "unlock", defaultValue = "false") boolean unlock) {
        try {
            if (!unlock) {
                LockHolder.get().setResource(resource);
                LockHolder.get().setResource(node);
                boolean lock = mysqlDistributedLock.tryLock(Duration.ofSeconds(timeout));
                return "mysql lock-" + lock + "-" + LockHolder.get();
            } else {
                boolean unlock1 = mysqlDistributedLock.unlock();
                return "mysql unlock-" + unlock1 + "-" + LockHolder.get();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "OK";
    }

    /**
     * etcd 分布式锁
     *
     * @param resource
     * @param timeout
     * @param unlock
     * @return
     */
    @RequestMapping("etcd")
    public String etcdLock(
            @RequestParam("resource") String resource,
            @RequestParam(value = "node", defaultValue = "0") String node,
            @RequestParam(value = "timeout", defaultValue = "3") int timeout,
            @RequestParam(value = "unlock", defaultValue = "false") boolean unlock) {
        try {
            if (!unlock) {
                LockHolder.get().setResource(resource);
                LockHolder.get().setResource(node);
                boolean lock = etcdDistributedLock.tryLock(Duration.ofSeconds(timeout));
                return "lock-" + lock + "-" + LockHolder.get();
            } else {
                boolean unlock1 = etcdDistributedLock.unlock();
                return "unlock-" + unlock1 + "-" + LockHolder.get();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "OK";
    }


}
