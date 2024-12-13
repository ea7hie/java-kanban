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
    private final ArrayList<Task> viewedTasks = new ArrayList<>();
    private final Map<Integer, Node<Task>> indexesOfViewedTasks = new HashMap<>();

    @Override
    public void add(Task task) {
        viewedTasks.add(task);


    }

    @Override
    public ArrayList<Task> getHistory() {
        return viewedTasks;
    }

    @Override
    public void remove(int id) {
        System.out.println(123);
    }

    public void removeAllTasksInViewedTasks() {
        viewedTasks.removeIf(viewedTask -> !(viewedTask instanceof Subtask || viewedTask instanceof Epic));
    }

    public void removeAllEpicsInViewedTasks() {
        viewedTasks.removeIf(viewedTask -> viewedTask instanceof Epic);
        viewedTasks.removeIf(viewedTask -> viewedTask instanceof Subtask);
    }

    public void removeOneElem(int idForDelete) {
        viewedTasks.removeIf(viewedTask -> viewedTask.getId() == idForDelete);
    }

    public <T extends Task> void updateOneElem(T task) {
        for (Task viewedTask : viewedTasks) {
            if (viewedTask.getId() == task.getId()) {
                viewedTask.setName(task.getName());
                viewedTask.setDescription(task.getDescription());
                viewedTask.setStatus(task.getStatus());
            }
        }
    }

    public void clearListOfViewedTasks() {
        viewedTasks.clear();
    }

    void addNewNode(Node<Task> node) {
        indexesOfViewedTasks.put(node.data.getId(), node);
    }


    class DoubleLinkedList{
        Node<Task> head;
        Node<Task> tail;
        private int size = 0;
        private final ArrayList<Task> allViewedTasks = new ArrayList<>();

        public void linkLast(Task element) {
            size++;
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

        public int getSize() {
            return size;
        }
    }
}


