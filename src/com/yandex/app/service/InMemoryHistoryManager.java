package com.yandex.app.service;

import com.yandex.app.model.Epic;
import com.yandex.app.model.Subtask;
import com.yandex.app.model.Task;
import com.yandex.app.service.interfaces.HistoryManager;
import com.yandex.app.service.model.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class InMemoryHistoryManager implements HistoryManager {
    private final Map<Integer, Node<Task>> indexesOfViewedTasks = new HashMap<>();
    DoubleLinkedList doubleLinkedList = new DoubleLinkedList();

    @Override
    public void add(Task task) {
        if (indexesOfViewedTasks.containsKey(task.getId())) {
            removeNode(indexesOfViewedTasks.get(task.getId()));
        }
        doubleLinkedList.linkLast(task);
    }

    @Override
    public ArrayList<Task> getHistory() {
        return doubleLinkedList.getTasks();
    }

    @Override
    public void remove(int id) {
        indexesOfViewedTasks.remove(id);
    }

    public void removeNode(Node<Task> curNode) {
        remove(curNode.data.getId());
        doubleLinkedList.allViewedTasks.remove(curNode.data);
        if (curNode == doubleLinkedList.head) {
            if (curNode.next != null) {
                curNode.next.prev = null;
                doubleLinkedList.head = curNode.next;
            } else {
                doubleLinkedList.head = null;
            }
        } else if (curNode == doubleLinkedList.tail) {
            curNode.prev.next = null;
            doubleLinkedList.tail = curNode.prev;
        } else {
            curNode.prev.next = curNode.next;
            curNode.next.prev = curNode.prev;
        }
    }

    public void removeAllTasksInViewedTasks() {
        ArrayList<Integer> idsForDelete = new ArrayList<>();
        for (Task viewedTask : doubleLinkedList.allViewedTasks) {
            if (!(viewedTask instanceof Subtask || viewedTask instanceof Epic)
                    && indexesOfViewedTasks.containsKey(viewedTask.getId())) {
                idsForDelete.add(viewedTask.getId());
            }
        }

        for (Integer i : idsForDelete) {
            removeNode(indexesOfViewedTasks.get(i));
        }
    }

    public void removeAllEpicsInViewedTasks() {
        ArrayList<Integer> idsForDelete = new ArrayList<>();
        for (Task viewedTask : doubleLinkedList.allViewedTasks) {
            if ((viewedTask instanceof Subtask || viewedTask instanceof Epic)
                    && indexesOfViewedTasks.containsKey(viewedTask.getId())) {
                idsForDelete.add(viewedTask.getId());
            }
        }

        for (Integer i : idsForDelete) {
            removeNode(indexesOfViewedTasks.get(i));
        }
    }

    public void removeOneElem(int idForDelete) {
        if (indexesOfViewedTasks.containsKey(idForDelete)) {
            removeNode(indexesOfViewedTasks.get(idForDelete));
        }
    }

    public <T extends Task> void updateOneElem(T task) {
        for (Task viewedTask : doubleLinkedList.allViewedTasks) {
            if (viewedTask.getId() == task.getId()) {
                viewedTask.setName(task.getName());
                viewedTask.setDescription(task.getDescription());
                viewedTask.setStatus(task.getStatus());
            }
        }

        Task oldTask = indexesOfViewedTasks.get(task.getId()).data;
        oldTask.setName(task.getName());
        oldTask.setDescription(task.getDescription());
        oldTask.setStatus(task.getStatus());
    }

    public void clearListOfViewedTasks() {
        doubleLinkedList.allViewedTasks.clear();
        indexesOfViewedTasks.clear();
        doubleLinkedList = new DoubleLinkedList();
    }

    void addNewNode(Node<Task> node) {
        indexesOfViewedTasks.put(node.data.getId(), node);
    }


    class DoubleLinkedList {
        Node<Task> head;
        Node<Task> tail;
        private final ArrayList<Task> allViewedTasks = new ArrayList<>();

        public void linkLast(Task element) {
            allViewedTasks.add(element);
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

        public ArrayList<Task> getTasks() {
            return this.allViewedTasks;
        }
    }
}


