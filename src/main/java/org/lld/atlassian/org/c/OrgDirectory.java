package org.lld.atlassian.org.c;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class OrgDirectory {

    static class Employee {
        String id;
        Employee(String id) { this.id = id; }
    }

    static class Group {
        String id;
        // DAG: multiple parents
        List<Group> parents = new ArrayList<>();
        Group(String id) { this.id = id; }
    }

    private final Map<String, Group> groups = new HashMap<>();
    private final Map<String, Employee> employees = new HashMap<>();
    // Employees can be in multiple groups
    private final Map<Employee, Set<Group>> empToGroups = new HashMap<>();

    // NEW: one readers–writer lock for the whole structure
    private final ReentrantReadWriteLock rw = new ReentrantReadWriteLock(true);

    Group ensureGroup(String groupId) {
        rw.writeLock().lock();
        try {
            return groups.computeIfAbsent(groupId, Group::new);
        } finally {
            rw.writeLock().unlock();
        }
    }

    Employee ensureEmployee(String empId) {
        rw.writeLock().lock();
        try {
            return employees.computeIfAbsent(empId, Employee::new);
        } finally {
            rw.writeLock().unlock();
        }
    }

    // Update #1: add or replace parent link (choose replace to stay minimal)
    void setParent(String childId, String parentId) {
        rw.writeLock().lock();
        try {
            Group child = groups.computeIfAbsent(childId, Group::new);
            Group parent = (parentId == null) ? null : groups.computeIfAbsent(parentId, Group::new);
            child.parents.clear();
            if (parent != null) child.parents.add(parent);
            // If you want additive parents (true DAG), replace the two lines above with:
            // if (parent != null && !child.parents.contains(parent)) child.parents.add(parent);
        } finally {
            rw.writeLock().unlock();
        }
    }

    // Update #2: assign employee to a group (multi-membership)
    void assignEmployeeToGroup(String empId, String groupId) {
        rw.writeLock().lock();
        try {
            Employee e = employees.computeIfAbsent(empId, Employee::new);
            Group g = groups.computeIfAbsent(groupId, Group::new);
            empToGroups.computeIfAbsent(e, k -> new HashSet<>()).add(g);
        } finally {
            rw.writeLock().unlock();
        }
    }

    // You can add similar write-locked methods for: removeParent, removeGroup, unassignEmployee, etc.

    // --- Internals (read-only helpers) ---
    private Map<Group, Integer> shortestUpwardDistances(Set<Group> starts) {
        // Called only under read lock from the public query
        Map<Group, Integer> dist = new HashMap<>();
        ArrayDeque<Group> q = new ArrayDeque<>();
        for (Group s : starts) {
            dist.put(s, 0);
            q.add(s);
        }
        while (!q.isEmpty()) {
            Group cur = q.poll();
            int d = dist.get(cur);
            for (Group p : cur.parents) {
                int nd = d + 1;
                Integer old = dist.get(p);
                if (old == null || nd < old) {
                    dist.put(p, nd);
                    q.add(p);
                }
            }
        }
        return dist;
    }

    // --- Query: ONE closest common ancestor in a DAG (same logic as (b)) ---
    Group getClosestCommonGroupForEmployees(Collection<String> employeeIds) {
        rw.readLock().lock();
        try {
            if (employeeIds == null || employeeIds.isEmpty()) return null;

            // Per-employee distance maps via multi-source upward BFS
            List<Map<Group, Integer>> perEmp = new ArrayList<>();
            for (String id : employeeIds) {
                Employee e = employees.get(id);
                if (e == null) throw new IllegalArgumentException("Unknown employee: " + id);
                Set<Group> starts = empToGroups.get(e);
                if (starts == null || starts.isEmpty())
                    throw new IllegalStateException("Employee not assigned to any group: " + id);
                // defensive copy so our traversal can’t be affected mid-iteration
                perEmp.add(shortestUpwardDistances(new HashSet<>(starts)));
            }

            // Intersect ancestors visible to all employees
            Set<Group> candidates = new HashSet<>(perEmp.get(0).keySet());
            for (int i = 1; i < perEmp.size(); i++) {
                candidates.retainAll(perEmp.get(i).keySet());
                if (candidates.isEmpty()) return null;
            }

            // Choose ONE closest: minimize max distance, then sum
            Group best = null;
            int bestMax = Integer.MAX_VALUE, bestSum = Integer.MAX_VALUE;
            for (Group g : candidates) {
                int maxD = 0, sumD = 0;
                for (Map<Group, Integer> m : perEmp) {
                    int d = m.get(g);
                    if (d > maxD) maxD = d;
                    sumD += d;
                }
                if (maxD < bestMax || (maxD == bestMax && sumD < bestSum)) {
                    best = g; bestMax = maxD; bestSum = sumD;
                }
            }
            return best;
        } finally {
            rw.readLock().unlock();
        }
    }

    // --- Demo ---
    public static void main(String[] args) {
        OrgDirectory org = new OrgDirectory();

        // Build small DAG
        org.setParent("B", "A");
        org.setParent("C", "A");
        org.setParent("D", "B");
        // Add another parent for D (switch setParent to additive if desired)
        org.rw.writeLock().lock();
        try {
            Group D = org.groups.computeIfAbsent("D", Group::new);
            Group C = org.groups.computeIfAbsent("C", Group::new);
            if (!D.parents.contains(C)) D.parents.add(C); // demo add
        } finally {
            org.rw.writeLock().unlock();
        }

        org.assignEmployeeToGroup("e1", "B");
        org.assignEmployeeToGroup("e2", "C");
        org.assignEmployeeToGroup("e3", "D");

        System.out.println(org.getClosestCommonGroupForEmployees(Arrays.asList("e1", "e2")).id); // A
        System.out.println(org.getClosestCommonGroupForEmployees(Arrays.asList("e1", "e3")).id); // B
    }
}