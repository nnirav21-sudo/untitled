package org.lld.atlassian.org.b;

import java.util.*;

public class OrgDirectory {

    static class Employee {
        String id;
        Employee(String id) { this.id = id; }
    }

    static class Group {
        String id;
        // CHANGED: multiple parents in a DAG
        List<Group> parents = new ArrayList<>();
        Group(String id) { this.id = id; }
    }

    Map<String, Group> groups = new HashMap<>();
    Map<String, Employee> employees = new HashMap<>();
    // CHANGED: allow multiple memberships per employee
    Map<Employee, Set<Group>> empToGroups = new HashMap<>();

    Group ensureGroup(String groupId) {
        return groups.computeIfAbsent(groupId, Group::new);
    }

    Employee ensureEmployee(String empId) {
        return employees.computeIfAbsent(empId, Employee::new);
    }

    // CHANGED: add (child <- parent) edge; no depth tracking needed in a DAG
    void setParent(String childId, String parentId) {
        Group child = ensureGroup(childId);
        Group parent = parentId == null ? null : ensureGroup(parentId);
        child.parents.clear();
        if (parent != null) child.parents.add(parent);
        // NOTE: if you want to *add* another parent instead of replacing, use:
        // if (parent != null && !child.parents.contains(parent)) child.parents.add(parent);
    }

    // CHANGED: add membership to a set
    void assignEmployeeToGroup(String empId, String groupId) {
        Employee e = ensureEmployee(empId);
        Group g = ensureGroup(groupId);
        empToGroups.computeIfAbsent(e, k -> new HashSet<>()).add(g);
    }

    // NEW: multi-source upward BFS to compute shortest distances to all ancestors
    private Map<Group, Integer> shortestUpwardDistances(Set<Group> starts) {
        Map<Group, Integer> dist = new HashMap<>();
        ArrayDeque<Group> dq = new ArrayDeque<>();
        for (Group s : starts) {
            dist.put(s, 0);
            dq.add(s);
        }
        while (!dq.isEmpty()) {
            Group cur = dq.poll();
            int d = dist.get(cur);
            for (Group p : cur.parents) {
                int nd = d + 1;
                Integer old = dist.get(p);
                if (old == null || nd < old) {
                    dist.put(p, nd);
                    dq.add(p);
                }
            }
        }
        return dist;
    }

    // CHANGED: works for DAG + multi-membership and returns ONE closest group
    Group getClosestCommonGroupForEmployees(Collection<String> employeeIds) {
        if (employeeIds == null || employeeIds.isEmpty()) return null;

        // Build per-employee distance maps (multi-source BFS from their groups)
        List<Map<Group, Integer>> perEmp = new ArrayList<>();
        for (String id : employeeIds) {
            Employee e = employees.get(id);
            if (e == null) throw new IllegalArgumentException("Unknown employee: " + id);
            Set<Group> starts = empToGroups.get(e);
            if (starts == null || starts.isEmpty())
                throw new IllegalStateException("Employee not assigned to any group: " + id);
            perEmp.add(shortestUpwardDistances(starts));
        }

        // Intersect candidate groups (ancestors seen by all employees)
        Set<Group> candidates = new HashSet<>(perEmp.get(0).keySet());
        for (int i = 1; i < perEmp.size(); i++) {
            candidates.retainAll(perEmp.get(i).keySet());
            if (candidates.isEmpty()) return null; // no common ancestor
        }

        // Choose ONE closest: minimize max distance, then sum distance
        Group best = null;
        int bestMax = Integer.MAX_VALUE, bestSum = Integer.MAX_VALUE;
        for (Group g : candidates) {
            int maxD = 0, sumD = 0;
            for (Map<Group, Integer> m : perEmp) {
                int d = m.get(g);
                maxD = Math.max(maxD, d);
                sumD += d;
            }
            if (maxD < bestMax || (maxD == bestMax && sumD < bestSum)) {
                best = g; bestMax = maxD; bestSum = sumD;
            }
        }
        return best;
    }

    // --- Demo ---
    public static void main(String[] args) {
        OrgDirectory org = new OrgDirectory();

        // DAG:
        //    A
        //  /   \
        // B     C
        //  \   /
        //    D
        org.setParent("B", "A");
        org.setParent("C", "A");
        // add second parent for D (if replacing, use the "add" variant in setParent)
        org.setParent("D", "B");
        // To truly add another parent, swap setParent with:
        // org.ensureGroup("D").parents.add(org.ensureGroup("C"));

        // Employees in multiple groups
        org.assignEmployeeToGroup("e1", "B");
        org.assignEmployeeToGroup("e2", "C");
        org.assignEmployeeToGroup("e3", "D"); // shared child

        // Expected closest common:
        // e1 & e2 -> A
        // e1 & e3 -> B (D is descendant of B and C; upward from D reaches B & C; closest with e1@B is B)
        System.out.println(org.getClosestCommonGroupForEmployees(Arrays.asList("e1", "e2")).id); // A
        System.out.println(org.getClosestCommonGroupForEmployees(Arrays.asList("e1", "e3")).id); // B
    }
}
