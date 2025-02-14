package com.yandex.app.service;

import com.yandex.app.model.Epic;
import com.yandex.app.model.Subtask;
import com.yandex.app.model.Task;
import com.yandex.app.service.interfaces.HistoryManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class InMemoryHistoryManager implements HistoryManager {
    private final DoubleLinkedList doubleLinkedList = new DoubleLinkedList();
    private FileBackedTaskManager fm;

    public void setFm(FileBackedTaskManager fm) {
        this.fm = fm;
    }

    public void recordFromFBM(Task task) {
        if (doubleLinkedList.indexesOfViewedTasks.containsKey(task.getId())) {
            remove(task.getId());
        }
        doubleLinkedList.linkLast(task);
    }

    @Override
    public void add(Task task) {
        if (doubleLinkedList.indexesOfViewedTasks.containsKey(task.getId())) {
            remove(task.getId());
        }
        doubleLinkedList.linkLast(task);
        if (fm != null) {
            fm.needSave();
        }
    }

    @Override
    public ArrayList<Task> getHistory() {
        return doubleLinkedList.getTasks();
    }

    @Override
    public void remove(int id) {
        if (doubleLinkedList.indexesOfViewedTasks.containsKey(id)) {
            doubleLinkedList.removeNode(doubleLinkedList.indexesOfViewedTasks.get(id));
            doubleLinkedList.removeOneElemFromMap(id);
            if (fm != null) {
                fm.needSave();
            }
        }
    }

    public void removeAllTasksInViewedTasks() {
        for (Integer id : doubleLinkedList.findIdsOfAllTasksInViewedTasks()) {
            remove(id);
        }
        if (fm != null) {
            fm.needSave();
        }
    }

    public void removeAllEpicsInViewedTasks() {
        for (Integer id : doubleLinkedList.findIdsOfAllEpicsInViewedTasks()) {
            remove(id);
        }
        if (fm != null) {
            fm.needSave();
        }
    }

    public void clearListOfViewedTasks() {
        doubleLinkedList.clear();
        if (fm != null) {
            fm.needSave();
        }
    }

    private class DoubleLinkedList {
        private Node<Task> head;
        private Node<Task> tail;
        private final Map<Integer, Node<Task>> indexesOfViewedTasks = new HashMap<>();

        private static class Node<T> {
            private T data;
            private Node<T> next;
            private Node<T> prev;

            public Node(Node<T> prev, T data, Node<T> next) {
                this.data = data;
                this.next = next;
                this.prev = prev;
            }

            @Override
            public String toString() {
                return data.toString();
            }
        }

        private void linkLast(Task element) {
            Node<Task> newNode;
            if (head == null) {
                newNode = new Node<>(null, element, null);
                head = newNode;
                addNewNode(newNode);
                return;
            }
            if (tail == null) {
                newNode = new Node<>(head, element, null);
                head.next = newNode;
                tail = newNode;
                addNewNode(newNode);
                return;
            }
            final Node<Task> oldTail = tail;
            newNode = new Node<>(oldTail, element, null);
            oldTail.next = newNode;
            tail = newNode;
            addNewNode(newNode);
        }

        private ArrayList<Task> getTasks() {
            ArrayList<Task> allViewedTasks = new ArrayList<>();

            if (head == null) {
                return allViewedTasks;
            }

            Node<Task> curNode = head;
            do {
                allViewedTasks.add(curNode.data);
                if (curNode.next != null) {
                    curNode = curNode.next;
                }
            } while (curNode.next != null);

            if (tail != null) {
                allViewedTasks.add(tail.data);
            }

            return allViewedTasks;
        }

        private void clear() {
            head = null;
            tail = null;
            indexesOfViewedTasks.clear();
        }

        private void addNewNode(Node<Task> node) {
            indexesOfViewedTasks.put(node.data.getId(), node);
        }

        private void removeNode(Node<Task> curNode) {
            if (curNode == doubleLinkedList.head) {
                if (curNode.next == null) {
                    doubleLinkedList.head = null;
                } else {
                    curNode.next.prev = null;
                    doubleLinkedList.head = curNode.next;
                }
            } else if (curNode == doubleLinkedList.tail) {
                curNode.prev.next = null;
                doubleLinkedList.tail = curNode.prev;
            } else {
                curNode.prev.next = curNode.next;
                curNode.next.prev = curNode.prev;
            }
        }

        private ArrayList<Integer> findIdsOfAllTasksInViewedTasks() {
            ArrayList<Integer> idsForDelete = new ArrayList<>();
            for (Node<Task> curNode : doubleLinkedList.indexesOfViewedTasks.values()) {
                if (!(curNode.data instanceof Subtask || curNode.data instanceof Epic)) {
                    idsForDelete.add(curNode.data.getId());
                }
            }
            return idsForDelete;
        }

        private ArrayList<Integer> findIdsOfAllEpicsInViewedTasks() {
            ArrayList<Integer> idsForDelete = new ArrayList<>();
            for (Node<Task> curNode : doubleLinkedList.indexesOfViewedTasks.values()) {
                if (curNode.data instanceof Subtask || curNode.data instanceof Epic) {
                    idsForDelete.add(curNode.data.getId());
                }
            }
            return idsForDelete;
        }

        private void removeOneElemFromMap(int id) {
            indexesOfViewedTasks.remove(id);
        }
    }
}