package org.lld.atlassian;

import java.util.*;

public class OrgDirectory {

    static class Employee {
        String id;
        Employee(String id) { this.id = id; }
    }

    static class Group {
        String id;
        Group parent;
        int depth;
        Group(String id) { this.id = id; }
    }

    Map<String, Group> groups = new HashMap<>();
    Map<String, Employee> employees = new HashMap<>();
    Map<Employee, Group> empToGroup = new HashMap<>();

    Group ensureGroup(String groupId) {
        return groups.computeIfAbsent(groupId, Group::new);
    }

    Employee ensureEmployee(String empId) {
        return employees.computeIfAbsent(empId, Employee::new);
    }

    void setParent(String childId, String parentId) {
        Group child = ensureGroup(childId);
        Group parent = parentId == null ? null : ensureGroup(parentId);
        child.parent = parent;
        child.depth = (parent == null) ? 0 : parent.depth + 1;
    }

    void assignEmployeeToGroup(String empId, String groupId) {
        Employee e = ensureEmployee(empId);
        Group g = ensureGroup(groupId);
        empToGroup.put(e, g);
    }

    Group getClosestCommonGroupForEmployees(Collection<String> employeeIds) {
        if (employeeIds == null || employeeIds.isEmpty()) return null;

        // Map employees to their groups
        List<Group> employeeGroups = new ArrayList<>();
        for (String id : employeeIds) {
            Employee e = employees.get(id);
            if (e == null) throw new IllegalArgumentException("Unknown employee: " + id);
            Group g = empToGroup.get(e);
            if (g == null) throw new IllegalStateException("Employee not assigned: " + id);
            employeeGroups.add(g);
        }

        // Collect ancestors of the first employee
        Set<Group> common = new HashSet<>();
        Group cur = employeeGroups.get(0);
        while (cur != null) {
            common.add(cur);
            cur = cur.parent;
        }

        // Intersect with others' ancestors
        for (int i = 1; i < employeeGroups.size(); i++) {
            Set<Group> anc = new HashSet<>();
            cur = employeeGroups.get(i);
            while (cur != null) {
                anc.add(cur);
                cur = cur.parent;
            }
            common.retainAll(anc);
        }

        // Find deepest group
        Group best = null;
        int bestDepth = -1;
        for (Group g : common) {
            if (g.depth > bestDepth) {
                best = g;
                bestDepth = g.depth;
            }
        }
        return best;
    }

    public static void main(String[] args) {
        OrgDirectory org = new OrgDirectory();

        // Build org hierarchy
        org.setParent("A", null);
        org.setParent("B", "A");
        org.setParent("C", "A");
        org.setParent("D", "B");
        org.setParent("E", "B");
        org.setParent("F", "C");

        // Assign employees
        org.assignEmployeeToGroup("e1", "D");
        org.assignEmployeeToGroup("e2", "E");
        org.assignEmployeeToGroup("e3", "F");
        org.assignEmployeeToGroup("e4", "B");

        // Test cases
        System.out.println(org.getClosestCommonGroupForEmployees(Arrays.asList("e1", "e2")).id); // B
        System.out.println(org.getClosestCommonGroupForEmployees(Arrays.asList("e1", "e3")).id); // A
        System.out.println(org.getClosestCommonGroupForEmployees(Arrays.asList("e1", "e4")).id); // B
    }
}
