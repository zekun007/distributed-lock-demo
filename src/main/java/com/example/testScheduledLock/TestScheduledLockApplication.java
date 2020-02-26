package com.example.testScheduledLock;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
@EnableScheduling
@SpringBootApplication
public class TestScheduledLockApplication {

	public static void main(String[] args) {
		SpringApplication.run(TestScheduledLockApplication.class, args);
		log.info("========================服务启动完成========================");
	}

}
