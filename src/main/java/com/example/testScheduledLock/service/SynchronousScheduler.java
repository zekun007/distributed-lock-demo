package com.example.testScheduledLock.service;


import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.SchedulerLock;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.redis.util.RedisLockRegistry;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

@Component
@Slf4j
public class SynchronousScheduler {
    //    -Dserver.port=8081
    @Value("${server.port}")
    private String port;

    @Autowired
    private RedisLockRegistry redisLockRegistry;

    private boolean flag = true;

    /**
     * lockAtLeastForString的作用是为了防止在任务开始之初由于各个服务器同名任务的服务器时间差，启动时间差等这些造成的一些问题，有了这个时间设置后，
     *     就可以避免因为上面这些小的时间差造成的一些意外，保证一个线程在抢到锁后，即便很快执行完，也不要立即释放，留下一个缓冲时间。
     *     这样等多个线程都启动后，由于任务已经被锁定，其他没有获得锁的任务也不会再去抢锁。注意这里的时间不要设置几秒几分钟，尽量大些
     *lockAtMostForString 这个设置的作用是为了防止抢到锁的那个线程，因为一些意外死掉了，而锁又始终不被释放。
     *     这样的话，虽然当前执行周期虽然失败了，但以后的执行周期如果这里一直不释放的话，后面就永远执行不到了。
     *     它的目的不在于隐藏任务，更重要的是，释放锁，并且查找解决问题。
     *至于是否带有string后缀，只是2种表达方式，数字类型的就是毫秒数，字符串类型的就有自己固定的格式 ，例如：PT30S  30s时间设置，单位可以是S,M,H
     */
    @Scheduled(cron = "0 */1 * * * ?")
//    @SchedulerLock(name = "scheduledController_notice", lockAtLeastForString = "PT2M", lockAtMostForString = "PT2M")
    public void notice() {
        try {
            log.info("＝＝{}＝{} 执行定时器 scheduledController_notice", getCurrentTime(), port);
        } catch (Exception e) {
            log.error("异常信息:", e);
        }
    }

    @Scheduled(cron = "*/1 * * * * ?")
    public void testRedisLockRegistry() {
        if (flag) {
            test();
        }
    }

    public void test() {
        flag = false;
        for(int i = 0; i < 100; i++) {
            try {
                String lockKey = "lock-" + i;
                log.info("＝＝{}＝{} 执行test | lockKey={}", getCurrentTime(), port, lockKey);
                Lock lock = redisLockRegistry.obtain(lockKey);
                boolean b1 = lock.tryLock(10, TimeUnit.SECONDS);
                log.info("＝＝{}＝{} 执行test | lockKey={} | b={}", getCurrentTime(), port, lockKey, b1);
                if (b1) {
                    TimeUnit.MINUTES.sleep(10);
                    log.info("＝＝{}＝{} 执行test | lockKey={} 任务执行完毕", getCurrentTime(), port, lockKey);
                    lock.unlock();
                    log.info("＝＝{}＝{} 执行test | lockKey={} 锁已删除", getCurrentTime(), port, lockKey);
                }
            } catch (InterruptedException e) {

            }
        }
    }

    private String getCurrentTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date now = new Date();
        return simpleDateFormat.format(now);
    }
}