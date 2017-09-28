package com.ddtc.singleredisdistributedlock.lock;

import redis.clients.jedis.Jedis;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Administrator on 2017/9/21.
 */
public class ExpiredManager {

    private static boolean isStart = false;

    private static Jedis jedis;

    private static ConcurrentHashMap<String,CopyOnWriteArrayList<ExpiredListener>> locks = new ConcurrentHashMap<>();

    public static void add(String key,ExpiredListener listener){
        CopyOnWriteArrayList<ExpiredListener> copyOnWriteArrayList = locks.get(key);
        if(copyOnWriteArrayList==null){
            copyOnWriteArrayList = new CopyOnWriteArrayList<ExpiredListener>();
            copyOnWriteArrayList.add(listener);
            locks.put(key,copyOnWriteArrayList);
        }else {
            copyOnWriteArrayList.add(listener);
        }

    }

    public static void remove(String key,ExpiredListener listener){
        CopyOnWriteArrayList<ExpiredListener> copyOnWriteArrayList = locks.get(key);
        if(copyOnWriteArrayList!=null){
            copyOnWriteArrayList.remove(listener);
        }
    }

    public synchronized static void start(){

        if(!isStart) {
            isStart = true;
            jedis = new Jedis("localhost", 16379);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        jedis.psubscribe(new ExpiredSub(locks), "__key*__:expired");
                    }catch (Exception e){
                        System.out.println(e.getMessage());
                    }
                }
            }).start();

        }
    }

    public synchronized static void close(){
        if(isStart) {
            isStart = false;
            jedis.close();
        }
    }

}
