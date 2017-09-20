package com.ddtc.singleredisdistributedlock.lock;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Administrator on 2017/9/13.
 */
public class DistributeLockRepository {

    private String host;
    private int port;
    private int maxTotal;
    private JedisPool jedisPool;


    /**
     *
     * @param host redis地址
     * @param port 端口
     * @param maxTotal 锁的最大个数，也就是说最多有maxTotal个线程能同时操作锁
     *
     **/
    public DistributeLockRepository(String host,int port,int maxTotal){
        this.host = host;
        this.port = port;
        this.maxTotal = maxTotal;

        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(maxTotal);
        jedisPool = new JedisPool(jedisPoolConfig, host, port);
    }




    public DistributeLock instance(String lockname) {


        Jedis jedis = jedisPool.getResource();
        // 若超过最大连接数，会在这里阻塞
        return new DistributeLock(jedis, lockname);


    }




}
