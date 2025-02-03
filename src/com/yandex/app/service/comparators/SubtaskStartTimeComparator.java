package com.yandex.app.service.comparators;

import com.yandex.app.model.Subtask;

import java.time.LocalDateTime;
import java.util.Comparator;

import static com.yandex.app.model.Task.FORMAT_DATE;

public class SubtaskStartTimeComparator implements Comparator<Subtask> {
    @Override
    public int compare(Subtask subtask1, Subtask subtask2) {
        return LocalDateTime.parse(subtask1.getStartTime(), FORMAT_DATE)
                .isAfter(LocalDateTime.parse(subtask2.getStartTime(), FORMAT_DATE)) ? 1
                : LocalDateTime.parse(subtask1.getStartTime(), FORMAT_DATE)
                .isBefore(LocalDateTime.parse(subtask2.getStartTime(), FORMAT_DATE)) ? -1 : 0;
    }
}