package com.ddtc.singleredisdistributedlock.lock;

import redis.clients.jedis.Jedis;

import java.util.Date;

/**
 * Created by Administrator on 2017/9/13.
 */
public class DistributeLock {


    private Jedis redisClient = null;
    private String key = ""; //锁的key
    private int expire = 0; //Redis自动删除时间
    private long startTime = 0L; //尝试获取锁的开始时间
    private long lockTime = 0L; //获取到锁的时间
    private boolean lock = false; //锁状态


    private static String script =
            "if redis.call('setnx',KEYS[1],KEYS[2]) == 1 then\n"
            +"redis.call('expire',KEYS[1],KEYS[3]);\n"
            +"return 1;\n"
            +"else\n"
            +"return 0;\n"
            +"end\n";

    DistributeLock(Jedis jedis, String key) {
        this.redisClient = jedis;
        this.key = key;
    }

    /**
     * @param timeout 锁阻塞超时的时间  单位：毫秒
     * @param expire  redis锁超时自动删除的时间   单位：秒
     * @return true-加锁成功  false-加锁失败
     */

    public boolean lock(long timeout, int expire) {
        this.expire = expire;
        this.startTime = System.currentTimeMillis();
        try {
            //在timeout的时间范围内不断轮询锁

            while (System.currentTimeMillis() - startTime < timeout) {
                //锁不存在的话，设置锁并设置锁过期时间，即加锁，并把value 定义为过期的时间点
                //this.redisClient.setnx(this.key, String.valueOf(System.currentTimeMillis() + expire * 1000)) == 1
                //this.redisClient.expire(key, expire);//设置锁过期时间是为了在没有释放

                //使用Lua脚本保证setnx与expire的原子性
                Object object = redisClient.eval(script,3,key,"a",String.valueOf(expire));
                if ((long)object==1) {
                        this.lockTime = System.currentTimeMillis();
                        //锁的情况下锁过期后消失，不会造成永久阻塞
                        this.lock = true;
                        System.out.println(key + "加锁成功" + Thread.currentThread().getName());
                        return this.lock;

                }
//                else {
//                    //如果Redis在超时后没有自动删除，则删除。。用于解决setnx 与 expire 不是原子操作的问题
//                    if (Long.valueOf(redisClient.get(key)) - System.currentTimeMillis() < 0) {
//                        redisClient.del(key);
//                    }
//                }
                System.out.println("出现锁等待" + Thread.currentThread().getName());
                //短暂休眠，避免可能的活锁
                Thread.sleep(500);
            }
            System.out.println("锁超时"+Thread.currentThread().getName());
        } catch (Exception e) {
            throw new RuntimeException("locking error", e);
        }
        this.lock = false;
        return this.lock;


    }

    public boolean unlock() {

        if (this.lock) {
            //解决在 Redis自动删除锁后，尝试解锁的问题
            if (System.currentTimeMillis() - lockTime <= expire) {
                redisClient.del(key);//直接删除  如果没有key，也没关系，不会有异常
            }
            this.lock = false;
            redisClient.close();//关闭连接
            return true;
        }

        return false;

    }


}
