package org.lld.atlassian.stringpopularity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AllOne {

    private static class Node {
        int freq;
        Node prev, next;
        Set<String> keys = new HashSet<>();

        Node(int freq) {
            this.freq = freq;
        }
    }

    private final Node head;                 // Dummy head
    private final Node tail;                 // Dummy tail
    private final Map<String, Node> map;     // key -> node

    /** Initialize your data structure here. */
    public AllOne() {
        head = new Node(0);
        tail = new Node(0);
        head.next = tail;
        tail.prev = head;
        map = new HashMap<>();
    }

    /** Inserts a new key <Key> with value 1. Or increments an existing key by 1. */
    public void inc(String key) {
        if (map.containsKey(key)) {
            Node node = map.get(key);
            int freq = node.freq;
            node.keys.remove(key);

            Node nextNode = node.next;
            if (nextNode == tail || nextNode.freq != freq + 1) {
                // create new node with freq+1 right after current node
                Node newNode = new Node(freq + 1);
                newNode.keys.add(key);
                insertAfter(node, newNode);
                map.put(key, newNode);
            } else {
                // reuse existing next node
                nextNode.keys.add(key);
                map.put(key, nextNode);
            }

            // remove empty node
            if (node.keys.isEmpty()) removeNode(node);

        } else {
            // key doesn't exist; place into freq=1 node after head (create if needed)
            Node first = head.next;
            if (first == tail || first.freq > 1) {
                Node newNode = new Node(1);
                newNode.keys.add(key);
                insertAfter(head, newNode);
                map.put(key, newNode);
            } else {
                first.keys.add(key);
                map.put(key, first);
            }
        }
    }

    /** Decrements an existing key by 1. If Key's value is 1, remove it from the data structure. */
    public void dec(String key) {
        Node node = map.get(key);
        if (node == null) return; // key not present

        node.keys.remove(key);
        int freq = node.freq;

        if (freq == 1) {
            // remove key entirely
            map.remove(key);
        } else {
            Node prevNode = node.prev;
            if (prevNode == head || prevNode.freq != freq - 1) {
                // create new node with freq-1 before current node
                Node newNode = new Node(freq - 1);
                newNode.keys.add(key);
                insertAfter(prevNode, newNode);
                map.put(key, newNode);
            } else {
                // reuse existing prev node
                prevNode.keys.add(key);
                map.put(key, prevNode);
            }
        }

        // remove empty node
        if (node.keys.isEmpty()) removeNode(node);
    }

    /** Returns one of the keys with maximal value. */
    public String getMaxKey() {
        if (tail.prev == head) return "";
        // return any key from the highest-frequency node
        return tail.prev.keys.iterator().next();
    }

    /** Returns one of the keys with minimal value. */
    public String getMinKey() {
        if (head.next == tail) return "";
        // return any key from the lowest-frequency node
        return head.next.keys.iterator().next();
    }

    // ----- helpers -----

    private void insertAfter(Node prev, Node node) {
        Node nxt = prev.next;
        prev.next = node;
        node.prev = prev;
        node.next = nxt;
        nxt.prev = node;
    }

    private void removeNode(Node node) {
        Node p = node.prev, n = node.next;
        p.next = n;
        n.prev = p;
        // no explicit delete needed in Java
    }
}
