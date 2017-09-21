package com.ddtc.singleredisdistributedlock.lock;

import redis.clients.jedis.JedisPubSub;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Administrator on 2017/9/21.
 */
public class ExpiredSub extends JedisPubSub {

    private ConcurrentHashMap<String,CopyOnWriteArrayList<ExpiredListener>> locks = null;

    ExpiredSub(ConcurrentHashMap<String,CopyOnWriteArrayList<ExpiredListener>> locks){
        this.locks = locks;
    }

    @Override
    public void onMessage(String channel, String message) {
        System.out.println(message);
    }

    @Override
    public void onPMessage(String pattern, String channel, String message) {

        for (ExpiredListener e: locks.get(message)
             ) {
            e.onExpired();
        }


    }
}
