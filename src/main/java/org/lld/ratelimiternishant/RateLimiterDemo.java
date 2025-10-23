package org.lld.ratelimiternishant;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


interface RateLimiter {
    public boolean allowRequest(String userId);
}

class TokenBucketRateLimiter implements RateLimiter {
    private final int maxToken;
    private final int refillRate;
    private Map<String,Long> lastRefillTime = new ConcurrentHashMap<>();
    private Map<String,Integer> tokens = new ConcurrentHashMap<>();
    public TokenBucketRateLimiter(int maxTokens, int refillRate) {
        this.maxToken = maxTokens;
        this.refillRate = refillRate;
    }
    @Override
    synchronized public boolean allowRequest(String userId) {
       long now = System.currentTimeMillis();
       lastRefillTime.putIfAbsent(userId,now);
       tokens.putIfAbsent(userId,maxToken);
       long lastRefillAT = lastRefillTime.get(userId);
       long elapsedTime = (now-lastRefillAT)/1000;

       if(elapsedTime>0){
          tokens.put(userId,Math.min(maxToken,tokens.get(userId)+(int)elapsedTime*refillRate)) ;
          lastRefillTime.put(userId,now);
       }
       if(tokens.get(userId)>0){
           tokens.put(userId,tokens.get(userId)-1);
           return true;
       }
       return false;
    }
}


public class RateLimiterDemo {
    public static void main(String[] args) throws InterruptedException {
        // Configure: 5 requests every 10 seconds
        RateLimiter limiter = new TokenBucketRateLimiter(5, 1);


        Runnable userTask = () -> {
            String userId = Thread.currentThread().getName();
            for (int i = 0; i < 10; i++) {
                boolean allowed = limiter.allowRequest(userId);
                System.out.println("User: " + userId
                        + " | Request " + (i + 1)
                        + " | Allowed: " + allowed
                        + " | Time: " + System.currentTimeMillis());
                try {
                    Thread.sleep(80);
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