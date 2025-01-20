package Utils;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class FileLock {
    private static final Lock lock = new ReentrantLock();

    public static void lock() {
        lock.lock();
    }

    public static void unlock() {
        lock.unlock();
    }
}
