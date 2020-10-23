package com.lb.redis;

import com.lb.redis.component.lock.RedisLock;
import com.lb.redis.service.RedisServiceTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author : lb
 * @date : 2020/10/16 10:43
 * @description : redis分布式锁测试
 */
public class RedisLockTest extends RedisTestApplicationTests {

    private static RedisServiceTest redisServiceTest;
    @Autowired
    public void setRedisServiceTest(RedisServiceTest serviceTest){
        redisServiceTest = serviceTest;
    }

    private static RedisLock redisLock;
    @Autowired
    public void setRedisLock(RedisLock lock){
        redisLock = lock;
    }


    // 请求数量
    private static int requestNums = 10000;
    // 统计访问次数，线程原子对象
    private static AtomicInteger num = new AtomicInteger(0);

    @Test
    public void redisLockTest() throws Exception {
        // 不加任何锁
//        noRedisLock();
        // 使用redisLock
        redisLock();
        // 使用java synchronized
//        synchronizedLock();
    }

    /**
     * 没有使用分布式锁，并发买票, 不安全
     * */
    private static void noRedisLock() throws Exception {
        // 机器性能允许的情况可以把核心线程数设置50或更大
        ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(8, 16,
                60,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(100000)
                );
        CountDownLatch cdl = new CountDownLatch(requestNums);
        for(int i = 0; i < requestNums; i++){
            poolExecutor.execute( () -> {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                int count = (int) redisServiceTest.get("count");
                count--;
                redisServiceTest.set("count", count);
                System.out.println("当前线程: "+ Thread.currentThread().getName()
                        + ", 当前票数: " + count);
                System.out.println("num: " + num.incrementAndGet());
                cdl.countDown();
            });
        }
        cdl.await();
        System.out.println("主线程执行完毕");
    }

    /**
     * 使用redis分布式锁，并发买票， 安全
     * */
    private static void redisLock() throws Exception {

        ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(10, 16,
                60,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(100000)
        );
        CountDownLatch cdl = new CountDownLatch(requestNums);
        Long startTime = System.currentTimeMillis();
        for(int i = 0; i < requestNums; i++){
            poolExecutor.execute( () -> {
//                while(redisServiceTest.buyTicketByLuaLock() == 0){
                while(redisServiceTest.buyTicketByRedLock() == 0){
                    System.out.println("线程: "+ Thread.currentThread().getName() + " 等待获取锁");
                }
                System.out.println("num: " + num.incrementAndGet());
                cdl.countDown();
            });
        }
        cdl.await();
        Long endTime = System.currentTimeMillis();
        System.out.println("主线程执行完毕 花费时长: " + ((endTime-startTime)/1000)+"s");
    }

    /**
     * 使用java synchronized 锁，并发买票， 安全
     * */
    private static void synchronizedLock() throws Exception {

        ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(10, 16,
                60,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(100000)
        );
        CountDownLatch cdl = new CountDownLatch(requestNums);
        Long startTime = System.currentTimeMillis();
        for(int i = 0; i < requestNums; i++){
            poolExecutor.execute( () -> {
                System.out.println("num: " + num.incrementAndGet());
                redisServiceTest.buyTicket2();
                cdl.countDown();
            });
        }
        cdl.await();
        Long endTime = System.currentTimeMillis();
        System.out.println("主线程执行完毕 花费时长: " + ((endTime-startTime)/1000)+"s");
    }

}
