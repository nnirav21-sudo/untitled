package org.lld.lruCache;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class LruCache {
    private class Node{
        int key;
        int value;
        Node prev;
        Node next;
        Node(int key,int value){
            this.key = key;
            this.value = value;
        }
    }
    private int capacity;
    private Map<Integer,Node> cacheMap;
    private LinkedList<Node> cacheList;
    Node head,tail;
    public LruCache(int capacity){
        this.capacity = capacity;
        this.cacheMap = new HashMap<>();
        this.cacheList = new LinkedList<>();
        this.head = new Node(0,0);
        this.tail = new Node(0,0);
        head.next = tail;
        tail.prev=head;
    }
    public int get(int key){
        System.out.println("Get called for key: " + key);
        if(!cacheMap.containsKey(key)){
            System.out.println("Key not found: " + key);
            return -1;
        }
        Node node = cacheMap.get(key);
        remove(node);
        addToHead(node);
        System.out.println("Key found: " + key + " value: " + node.value);
        printCacheState();
        return node.value;
    }
    public void put(int key,int value){
        System.out.println("Put called for key: " + key + ", value: " + value);
        if(cacheMap.containsKey(key)){
            remove(cacheMap.get(key));
        }else if(cacheMap.size() == capacity){
            System.out.println("Cache full. Removing LRU key: " + tail.prev.key);
            cacheMap.remove(tail.prev.key);
            remove(tail.prev);
        }
        Node node = new Node(key,value);
        cacheMap.put(key,node);
        addToHead(node);
        printCacheState();
    }

    void remove(Node node){
        node.next.prev=node.prev;
        node.prev.next=node.next;
    }
    void addToHead(Node node){
        node.next=head.next;
        node.prev=head;
        head.next=node;
        node.next.prev=node;
    }

    void printCacheState(){
        Node curr = head.next;
        System.out.print("Cache state (LRU): ");
        while(curr != tail){
            System.out.print("[" + curr.key + ":" + curr.value + "] ");
            curr = curr.next;
        }
        System.out.println();
    }

    public static void main(String[] args){
        LruCache lruCache = new LruCache(2);
        lruCache.put(0,1);
        lruCache.put(1,2);
        lruCache.get(0);
        lruCache.put(2,3);
        lruCache.put(3,4);
        lruCache.get(1);
    }
}
