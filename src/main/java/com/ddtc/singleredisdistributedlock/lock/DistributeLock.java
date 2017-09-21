package com.ddtc.singleredisdistributedlock.lock;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;


/**
 * Created by Administrator on 2017/9/13.
 */
public class DistributeLock implements ExpiredListener {


    private Jedis redisClient = null;
    private String key = ""; //锁的key
    private int expire = 0; //Redis自动删除时间
    private long startTime = 0L; //尝试获取锁的开始时间
    private long lockTime = 0L; //获取到锁的时间
    private boolean lock = false; //锁状态

    public void setLock(boolean lock) {
        this.lock = lock;
    }

    public void closeClient() {
        redisClient.close();
    }

    private static String script =
            "if redis.call('setnx',KEYS[1],KEYS[2]) == 1 then\n"
                    + "redis.call('expire',KEYS[1],KEYS[3]);\n"
                    + "return 1;\n"
                    + "else\n"
                    + "return 0;\n"
                    + "end\n";

    DistributeLock(Jedis jedis, String key) {
        this.redisClient = jedis;
        this.key = key;
    }


    @Override
    public void onExpired() {
        ExpiredManager.remove(key,this);
        this.setLock(false);

        redisClient.close();//关闭连接
        redisClient = null;
        System.out.println(key + "Redis超时自动解锁" + Thread.currentThread().getName());
    }

    //redisClient.psubscribe(new ExpiredSub(this),"__key*__:expired");


    /**
     * @param timeout 锁阻塞超时的时间  单位：毫秒
     * @param expire  redis锁超时自动删除的时间   单位：秒
     * @return true-加锁成功  false-加锁失败
     */

    public synchronized boolean lock(long timeout, int expire) {
        this.expire = expire;
        this.startTime = System.currentTimeMillis();


        if (!lock) {
            //System.out.println(Thread.currentThread().getName() + lock);
            try {
                //在timeout的时间范围内不断轮询锁
                while (System.currentTimeMillis() - startTime < timeout) {
                    //System.out.println(Thread.currentThread().getName() + "inWhile");
                    //使用Lua脚本保证setnx与expire的原子性
                    Object object = redisClient.eval(script, 3, key, "a", String.valueOf(expire));
                    //System.out.println(Thread.currentThread().getName() + "afterScript");
                    if ((long) object == 1) {
                        this.lockTime = System.currentTimeMillis();
                        //锁的情况下锁过期后消失，不会造成永久阻塞
                        this.lock = true;
                        System.out.println(key + "加锁成功" + Thread.currentThread().getName());
                        //交给超时管理器
                        ExpiredManager.add(key, this);
                        return this.lock;
                    }

                    System.out.println("出现锁等待" + Thread.currentThread().getName());
                    //短暂休眠，避免可能的活锁
                    Thread.sleep(500);
                }
                System.out.println("锁超时" + Thread.currentThread().getName());
            } catch (Exception e) {
                if(e instanceof NullPointerException){
                    throw new RuntimeException("无法对已经解锁后的锁重新加锁，请重新获取", e);
                }
                throw new RuntimeException("locking error", e);
            }
        } else {
            //System.out.println(key + "不可重入/用");
            throw new RuntimeException(key + "不可重入/用");

        }


        this.lock = false;
        return this.lock;


    }

    public synchronized void unlock() {

        if (this.lock) {
            //解决在 Redis自动删除锁后，尝试解锁的问题
            if (System.currentTimeMillis() - lockTime <= expire) {
                redisClient.del(key);//直接删除  如果没有key，也没关系，不会有异常

            }
            this.lock = false;
            redisClient.close();//关闭连接
            redisClient = null;

            System.out.println(key + "解锁成功" + Thread.currentThread().getName());


        }

    }


}
