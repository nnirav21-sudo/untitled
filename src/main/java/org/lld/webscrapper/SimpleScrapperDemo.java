package org.lld.webscrapper;

import java.util.*;
import java.util.concurrent.*;


public class SimpleScrapperDemo {


   static class WebScraper {
       private final int maxDepth;
       private final ExecutorService executor;


       public WebScraper(int maxDepth, int threads) {
           this.maxDepth = maxDepth;
           this.executor = Executors.newFixedThreadPool(threads);
       }


       public Map<String, Set<String>> scrape(List<String> seeds) throws InterruptedException {
           Map<String, Set<String>> results = new ConcurrentHashMap<>();
           CountDownLatch latch = new CountDownLatch(seeds.size());


           for (String seed : seeds) {
               executor.submit(() -> {
                   results.put(seed, crawl(seed));
                   latch.countDown();
               });
           }


           latch.await();
           executor.shutdown();
           return results;
       }


       private Set<String> crawl(String seed) {
           Set<String> images = ConcurrentHashMap.newKeySet();
           Set<String> visited = ConcurrentHashMap.newKeySet();
           Queue<UrlDepth> frontier = new ConcurrentLinkedQueue<>();


           frontier.add(new UrlDepth(seed, 0));
           visited.add(seed);


           while (!frontier.isEmpty()) {
               UrlDepth task = frontier.poll();
               if (task.depth > maxDepth) continue;


               // Dummy HTML (in real system, this is from HTTP call)
               String body = fetch(task.url);


               // Simplified "parsers"
               images.addAll(parseImages(body));
               for (String link : parseLinks(body)) {
                   if (visited.add(link)) {
                       frontier.add(new UrlDepth(link, task.depth + 1));
                   }
               }
           }
           return images;
       }


       // Fake fetch
       private String fetch(String url) {
           return "<html><body>"
                   + "<img>img1.png</img>"
                   + "<img>img2.png</img>"
                   + "<a>page2.html</a>"
                   + "</body></html>";
       }


       // Very simple mock parsers (no regex)
       private List<String> parseImages(String body) {
           return Arrays.asList("img1.png", "img2.png");
       }


       private List<String> parseLinks(String body) {
           return Arrays.asList("page2.html");
       }


       private static class UrlDepth {
           String url; int depth;
           UrlDepth(String url, int depth) { this.url = url; this.depth = depth; }
       }
   }


   // Demo
   public static void main(String[] args) throws Exception {
       WebScraper scraper = new WebScraper(1, 3); // depth=1, 3 threads
       List<String> seeds = Arrays.asList("http://start.com");


       Map<String, Set<String>> results = scraper.scrape(seeds);
       results.forEach((seed, imgs) -> {
           System.out.println("Seed: " + seed);
           imgs.forEach(img -> System.out.println("  " + img));
       });
   }
}

