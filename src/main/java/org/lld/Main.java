package org.lld;

import java.util.*;

class Employee {
    private final String name;
    private final Group group;

    public Employee(String name, Group group) {
        this.name = name;
        this.group = group;
    }

    public String getName() {
        return name;
    }

    public Group getGroup() {
        return group;
    }
}

class Group {
    private final String groupName;
    private final Set<Group> parentGroups = new HashSet<>();

    public Group(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupName() {
        return groupName;
    }

    public Set<Group> getParentGroups() {
        return parentGroups;
    }

    public void addParent(Group parent) {
        if (parent != null) parentGroups.add(parent);
    }

    @Override
    public String toString() {
        return groupName;
    }
}

public class Main {

    /**
     * Finds the lowest common group for a given set of employees.
     */
    private static Group getCommonGroup(Set<Employee> employees) {
        if (employees == null || employees.isEmpty()) {
            throw new IllegalArgumentException("Employee set cannot be empty");
        }

        // Map each group -> employees that belong (directly or indirectly)
        Map<Group, Set<Employee>> groupToEmployeeMap = new HashMap<>();

        // Traverse upward for each employee
        for (Employee employee : employees) {
            for (Group group : getGroupHierarchy(employee)) {
                groupToEmployeeMap
                        .computeIfAbsent(group, g -> new HashSet<>())
                        .add(employee);
            }
        }

        // Find all groups that cover ALL employees
        List<Group> commonGroups = new ArrayList<>();
        for (Map.Entry<Group, Set<Employee>> entry : groupToEmployeeMap.entrySet()) {
            if (entry.getValue().size() == employees.size()) {
                commonGroups.add(entry.getKey());
            }
        }

        if (commonGroups.isEmpty()) {
            throw new IllegalStateException("No common group found");
        }

        // Among common groups, pick the one with the greatest depth
        Group best = null;
        int bestDepth = -1;
        for (Group g : commonGroups) {
            int depth = getDepth(g);
            if (depth > bestDepth) {
                best = g;
                bestDepth = depth;
            }
        }
        return best;
    }

    /** Traverse upwards and get all ancestors (including self) */
    private static Set<Group> getGroupHierarchy(Employee employee) {
        Set<Group> visited = new HashSet<>();
        Deque<Group> queue = new ArrayDeque<>();
        queue.add(employee.getGroup());

        while (!queue.isEmpty()) {
            Group current = queue.poll();
            if (!visited.add(current)) continue; // already seen
            for (Group parent : current.getParentGroups()) {
                queue.add(parent);
            }
        }
        return visited;
    }

    /** Compute depth from the top (ROOT) by BFS */
    private static int getDepth(Group group) {
        // Depth = longest distance to root (no parent)
        Deque<Group> queue = new ArrayDeque<>();
        Map<Group, Integer> depth = new HashMap<>();
        queue.add(group);
        depth.put(group, 0);
        int maxDepth = 0;

        while (!queue.isEmpty()) {
            Group curr = queue.poll();
            int currDepth = depth.get(curr);
            for (Group parent : curr.getParentGroups()) {
                depth.put(parent, currDepth + 1);
                queue.add(parent);
                maxDepth = Math.max(maxDepth, currDepth + 1);
            }
        }
        return maxDepth;
    }

    public static void main(String[] args) {
        // Create groups
        Group company = new Group("Company");
        Group engg = new Group("Engineering");
        Group hr = new Group("HR");
        Group be = new Group("Backend");
        Group fe = new Group("Frontend");

        // Define parent relationships
        engg.addParent(company);
        hr.addParent(company);
        be.addParent(engg);
        fe.addParent(engg);

        // Employees
        Employee alice = new Employee("Alice", be);
        Employee bob = new Employee("Bob", be);
        Employee lisa = new Employee("Lisa", fe);
        Employee mona = new Employee("Mona", hr);

        Group result = getCommonGroup(Set.of(alice, bob, lisa, mona));
        System.out.println("The common group is: " + result.getGroupName());
    }
}
