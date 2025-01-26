package com.cgvsu.HistoryBuffer;

public class ActionHistory<T extends Action> {
    private static class ActionNode<T> {
        T action;
        ActionNode<T> prev;
        ActionNode<T> next;

        ActionNode(T action) {
            this.action = action;
        }

        @Override
        public String toString() {
            return "ActionNode{action=" + action + "}";
        }
    }

    private ActionNode<T> head;
    private ActionNode<T> tail;
    private ActionNode<T> current;
    private final int maxDepth;
    private int size;

    public ActionHistory(int maxDepth) {
        this.maxDepth = maxDepth;
        this.size = 0;
    }

    public void addAction(T action) {
        ActionNode<T> newNode = new ActionNode<>(action);

        if (head == null) {
            head = newNode;
            tail = newNode;
            current = head;
        } else {
            if (current.next != null) {
                current.next = null;
                tail = current;
            }

            newNode.prev = current;
            current.next = newNode;
            current = newNode;

            if (newNode.next == null) {
                tail = newNode;
            }
        }

        size++;
        if (size > maxDepth) {
            if (tail == current) {
                current = tail.prev;
            }
            tail = tail.prev;
            if (tail != null) {
                tail.next = null;
            }
            size--;
        }
    }

    public void undo() {
        if (current != null) {
            current.action.undo();
            current = current.prev;
        }
    }

    public void redo() {
        if (current != null && current.next != null) {
            current = current.next;
            current.action.redo();
        }
    }

    public void logHistory() {
        ActionNode<T> temp = head;
        while (temp != null) {
            System.out.print(temp.action + " -> ");
            temp = temp.next;
        }
        System.out.println("null");
    }
}
