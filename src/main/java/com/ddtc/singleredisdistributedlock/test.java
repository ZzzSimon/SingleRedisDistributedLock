package com.ddtc.singleredisdistributedlock;

import com.ddtc.singleredisdistributedlock.lock.DistributeLock;
import com.ddtc.singleredisdistributedlock.lock.DistributeLockRepository;

/**
 * Created by Administrator on 2017/9/13.
 */
public class test {
    public static void main(String[] args) {

        //第三个参数表示  同一时间 最多有多少锁能  处于加锁或者阻塞状态  其实就是连接池大小
        DistributeLockRepository distributeLockRepository = new DistributeLockRepository("localhost", 16379, 6);



        //lock1 忘记解锁，在5秒后，Redis自动解锁，lock2获取锁

        new Thread(new Runnable() {
            @Override
            public void run() {
                DistributeLock lock1 = distributeLockRepository.instance("lock[A]");
                lock1.lock(1000 * 20L, 5);
                try {
                    Thread.sleep(4000);
                    lock1.unlock();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        },"【1】").start();


        new Thread(new Runnable() {
            @Override
            public void run() {
                DistributeLock lock1 = distributeLockRepository.instance("lock[A]");
                lock1.lock(1000 * 20L, 5);
            }
        },"【2】").start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                DistributeLock lock1 = distributeLockRepository.instance("lock[A]");
                lock1.lock(1000 * 20L, 5);
            }
        },"【3】").start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                DistributeLock lock1 = distributeLockRepository.instance("lock[A]");
                lock1.lock(1000 * 20L, 5);
            }
        },"【4】").start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                DistributeLock lock1 = distributeLockRepository.instance("lock[A]");
                lock1.lock(1000 * 20L, 5);
            }
        },"【5】").start();


    }
}
