package priv.penuel.simple.lock.mysql;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import priv.penuel.simple.lock.Lock;
import priv.penuel.simple.lock.DistributedLock;
import priv.penuel.simple.lock.LockHolder;

import javax.annotation.Resource;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.locks.LockSupport;

/**
 * @author: penuel
 * @date: 2020-07-05 16:33
 * @desc: TODO
 */
@Component
public class MysqlDistributedLock implements DistributedLock {

    @Resource
    private JdbcTemplate jdbcTemplate;


    @Override
    public boolean tryLock(Duration timeout) {
        long start = Instant.now().toEpochMilli();
        while (true) {
            Lock lock = LockHolder.get();
            if (mysqlLock(lock.getResource(), lock.getNode())) {
                return true;
            }
            long end = Instant.now().toEpochMilli();
            if (end - start > timeout.toMillis()) {
                return false;
            }
            LockSupport.parkNanos(500 * 1000);
        }
    }


    @Override
    public boolean unlock() {
        Lock lock = LockHolder.get();
        return mysqlUnLock(lock.getResource(), lock.getNode());
    }

    @Transactional
    public boolean mysqlLock(String resource, String holderInfo) {
        String sql = " select * from lock where resource=" + resource + " for update ";
        Lock lock = jdbcTemplate.queryForObject(sql, Lock.class);
        if (null == lock) {
            String insertSql = " insert into lock (resource,node,count) values (" + resource + "," + holderInfo + ",1)";
            jdbcTemplate.execute(insertSql);
            return true;
        }
        if (holderInfo.equals(lock.getNode())) {
            String incCountSql = " update lock set count = count+1 where resource=" + resource + " and node=" + holderInfo;
            return jdbcTemplate.update(incCountSql) > 0;
        } else {
            return false;
        }
    }

    @Transactional
    public boolean mysqlUnLock(String resource, String node) {
        String sql = " select * from lock where resource=" + resource + " for update ";
        Lock lock = jdbcTemplate.queryForObject(sql, Lock.class);
        if (null == lock) { //maybe schedule job clean expired lock
            return true;
        }
        if (lock.getCount() > 1) {
            String incCountSql = " update lock set count = count-1 where resource=" + resource + " and node=" + node;
            return jdbcTemplate.update(incCountSql) > 0;
        } else {
            String delSql = " delete from lock where resource=" + resource + " and node=" + node;
            return jdbcTemplate.update(delSql) > 0;
        }
    }

}
