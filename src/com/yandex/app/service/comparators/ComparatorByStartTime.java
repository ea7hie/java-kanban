package com.yandex.app.service.comparators;

import com.yandex.app.model.Task;

import java.time.LocalDateTime;
import java.util.Comparator;

import static com.yandex.app.model.Task.FORMAT_DATE;

public class ComparatorByStartTime implements Comparator<Task> {
    @Override
    public int compare(Task task1, Task task2) {
        return LocalDateTime.parse(task1.getStartTime(), FORMAT_DATE)
                .isAfter(LocalDateTime.parse(task2.getStartTime(), FORMAT_DATE)) ? 1
                : LocalDateTime.parse(task1.getStartTime(), FORMAT_DATE)
                .isBefore(LocalDateTime.parse(task2.getStartTime(), FORMAT_DATE)) ? -1 : 0;
    }
}