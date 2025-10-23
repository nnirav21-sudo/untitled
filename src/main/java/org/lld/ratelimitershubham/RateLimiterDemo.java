package org.lld.ratelimiter;

import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.util.*;


// Rate Limiter Interface
interface RateLimiter {
   boolean allowRequest(String userId);
}


// Token Bucket Implementation
class TokenBucketRateLimiter implements RateLimiter {
   private final int maxTokens;
   private final long refillIntervalMillis;
   private final ConcurrentHashMap<String, UserBucket> userBuckets = new ConcurrentHashMap<>();


   // Inner class for user bucket
   private static class UserBucket {
       private final int maxTokens;
       private final long refillIntervalMillis;
       private double tokens;
       private long lastRefillTimestamp;
       private final Lock lock = new ReentrantLock();


       UserBucket(int maxTokens, long refillIntervalMillis) {
           this.maxTokens = maxTokens;
           this.refillIntervalMillis = refillIntervalMillis;
           this.tokens = maxTokens; // Start full
           this.lastRefillTimestamp = System.currentTimeMillis();
       }


       boolean tryConsume() {
           lock.lock();
           try {
               refillTokens();
               if (tokens >= 1) {
                   tokens -= 1;
                   return true;
               }
               return false;
           } finally {
               lock.unlock();
           }
       }


       private void refillTokens() {
           long now = System.currentTimeMillis();
           long elapsed = now - lastRefillTimestamp;
           if (elapsed > 0) {
               double tokensToAdd = (elapsed / (double) refillIntervalMillis) * maxTokens;
               tokens = Math.min(maxTokens, tokens + tokensToAdd);
               lastRefillTimestamp = now;
           }
       }
   }


   public TokenBucketRateLimiter(int maxRequests, int intervalSeconds) {
       this.maxTokens = maxRequests;
       this.refillIntervalMillis = intervalSeconds * 1000L;
   }


   @Override
   public boolean allowRequest(String userId) {
       UserBucket bucket = userBuckets.computeIfAbsent(
               userId,
               k -> new UserBucket(maxTokens, refillIntervalMillis)
       );
       return bucket.tryConsume();
   }
}


// Demo class
public class RateLimiterDemo {
   public static void main(String[] args) throws InterruptedException {
       // Configure: 5 requests every 10 seconds
       RateLimiter limiter = new TokenBucketRateLimiter(5, 10);


       Runnable userTask = () -> {
           String userId = Thread.currentThread().getName();
           for (int i = 0; i < 10; i++) {
               boolean allowed = limiter.allowRequest(userId);
               System.out.println("User: " + userId
                       + " | Request " + (i + 1)
                       + " | Allowed: " + allowed
                       + " | Time: " + System.currentTimeMillis());
               try {
                   Thread.sleep(1000);
               } catch (InterruptedException ignored) {}
           }
       };


       ExecutorService executor = Executors.newFixedThreadPool(2);
       executor.submit(userTask);
       executor.submit(userTask);


       executor.shutdown();
       executor.awaitTermination(1, TimeUnit.MINUTES);
   }
}

