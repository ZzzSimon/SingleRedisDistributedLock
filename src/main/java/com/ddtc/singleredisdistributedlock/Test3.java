package com.ddtc.singleredisdistributedlock;

import com.ddtc.singleredisdistributedlock.lock.DistributeLock;
import com.ddtc.singleredisdistributedlock.lock.DistributeLockRepository;
import com.ddtc.singleredisdistributedlock.lock.ExpiredManager;

/**
 * Created by Administrator on 2017/9/21.
 */
public class Test3 {
    public static void main(String[] args) {

        //第三个参数表示  同一时间 最多有多少锁能  处于加锁或者阻塞状态  其实就是连接池大小
        DistributeLockRepository distributeLockRepository = new DistributeLockRepository("localhost", 16379, 6);

        DistributeLock lock1 = distributeLockRepository.instance("lock[A]");
        DistributeLock lock2 = distributeLockRepository.instance("lock[A]");

        ExpiredManager.start();

        lock1.lock(1000 * 20L, 5);
        try {
            Thread.sleep(6000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        lock1.lock(1000 * 20L, 5);

        ExpiredManager.close();
    }
}
