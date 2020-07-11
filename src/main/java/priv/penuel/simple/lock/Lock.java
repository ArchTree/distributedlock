package priv.penuel.simple.lock;

import java.util.Date;

/**
 * @author: penuel
 * @date: 2020-07-09 17:35
 * @desc: TODO
 */
public class Lock {

    private String resource = "lock"; //当前持有的锁资源
    private String node = "0";

    private int count;
    private Date createTime;
    private Date updateTime;


    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }


    @Override
    public String toString() {
        return "Lock{" +
                "resource='" + resource + '\'' +
                ", node='" + node + '\'' +
                ", count=" + count +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}