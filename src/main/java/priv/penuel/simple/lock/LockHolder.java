package priv.penuel.simple.lock;

/**
 * @author: penuel
 * @date: 2020-07-09 16:50
 * @desc: TODO
 */
public class LockHolder {
    static final ThreadLocal<Lock> threadLocal = ThreadLocal.withInitial(() -> new Lock());

    public static Lock get() {
        return threadLocal.get();
    }

    public static void set(Lock lock) {
        threadLocal.set(lock);
    }

}
