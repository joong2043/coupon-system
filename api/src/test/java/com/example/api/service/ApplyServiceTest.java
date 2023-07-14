package com.example.api.service;

import com.example.api.repository.CouponRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.AssertionsForClassTypes.*;

@SpringBootTest
public class ApplyServiceTest {

    @Autowired
    private ApplyService applyService;

    @Autowired
    private CouponRepository couponRepository;

    @Test
    public void 한번만응모(){
        applyService.apply(1L);

        long count = couponRepository.count();

        assertThat(count).isEqualTo(1);
    }

    @Test
    public void 여러명응모() throws InterruptedException {
        int threadCount = 1000;

        // 병렬작업을 간단하게 할 수 있게 도와주는 자바 api
        ExecutorService executorService = Executors.newFixedThreadPool(32); // 32개의 스레드를 가진 스레드풀 생성

        // 다른 스레드에서 수행하는 작업을 기다리도록 도와줌
        CountDownLatch latch = new CountDownLatch(threadCount);

        // 1000개 요청 반복문
        for (int i = 0; i < threadCount; i++){
            long userId = i;

            executorService.submit(() -> {
                try {
                    applyService.apply(userId);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        Thread.sleep(10 * 1000);

        long count = couponRepository.count();

        assertThat(count).isEqualTo(100);
    }

    @Test
    public void 한명당_한개의쿠폰만_발급() throws InterruptedException {
        int threadCount = 1000;

        // 병렬작업을 간단하게 할 수 있게 도와주는 자바 api
        ExecutorService executorService = Executors.newFixedThreadPool(32); // 32개의 스레드를 가진 스레드풀 생성

        // 다른 스레드에서 수행하는 작업을 기다리도록 도와줌
        CountDownLatch latch = new CountDownLatch(threadCount);

        // 1000개 요청 반복문
        for (int i = 0; i < threadCount; i++){
            long userId = i;

            executorService.submit(() -> {
                try {
                    applyService.apply(1L);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        Thread.sleep(10 * 1000);

        long count = couponRepository.count();

        assertThat(count).isEqualTo(1);
    }
}
