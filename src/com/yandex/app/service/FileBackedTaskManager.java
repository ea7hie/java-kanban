package com.yandex.app.service;

import com.yandex.app.model.*;
import com.yandex.app.service.exceptions.ManagerSaveException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private Path fileForSaving;
    private final Path defaultPath;
    private final String headLine = "id,type,name,status,description,epic\n";
    private final InMemoryTaskManager inMemoryTaskManager;

    //создатели
    public FileBackedTaskManager(Path fileForSaving) {
        this.fileForSaving = fileForSaving;
        this.defaultPath = fileForSaving;
        inMemoryTaskManager = (InMemoryTaskManager) Managers.getDefaultTaskManager();
        inMemoryTaskManager.inMemoryHistoryManager.setFm(this);
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager fm = new FileBackedTaskManager(file.toPath());
        fm.recodeInArraysFromFile();
        return fm;
    }

    public InMemoryTaskManager getInMemoryTaskManager() {
        return inMemoryTaskManager;
    }

    //конвертеры
    private String fromTaskToString(Task task) {
        int indexOfType = task.getClass().toString().split("\\.").length - 1;
        String type = task.getClass().toString().split("\\.")[indexOfType].toUpperCase();

        if (Tasks.valueOf(type) == Tasks.SUBTASK) {
            Subtask subtask = (Subtask) task;
            String[] infoAboutTask = {String.valueOf(subtask.getId()),
                    Tasks.valueOf(type).toString(), subtask.getName(), subtask.getStatus().toString(),
                    subtask.getDescription(), String.valueOf(subtask.getIdOfSubtaskEpic()), subtask.getStartTime(),
                    String.valueOf(subtask.getDuration())
            };
            return String.join(",", infoAboutTask);
        } else if (Tasks.valueOf(type) == Tasks.EPIC) {
            String[] infoAboutTask = {String.valueOf(task.getId()), Tasks.valueOf(type).toString(),
                    task.getName(), task.getStatus().toString(), task.getDescription(), task.getStartTime(),
                    String.valueOf(task.getDuration()), ((Epic) task).getEndTimeForEpic()
            };
            return String.join(",", infoAboutTask);
        }

        String[] infoAboutTask = {String.valueOf(task.getId()), Tasks.valueOf(type).toString(),
                task.getName(), task.getStatus().toString(), task.getDescription(), task.getStartTime(),
                String.valueOf(task.getDuration())
        };
        return String.join(",", infoAboutTask);
    }

    private Task fromStringToTask(String taskInString) {
        String[] infoAboutTask = taskInString.split(",");

        switch (Tasks.valueOf(infoAboutTask[1])) {
            case Tasks.TASK:
                Task newTask = new Task(infoAboutTask[2], infoAboutTask[4], -1,
                        Integer.parseInt(infoAboutTask[6]), infoAboutTask[5]);
                newTask.setId(Integer.valueOf(infoAboutTask[0]));
                newTask.setStatus(Progress.valueOf(infoAboutTask[3]));
                return newTask;
            case Tasks.EPIC:
                Epic newEpic = new Epic(infoAboutTask[2], infoAboutTask[4], -1);
                newEpic.setId(Integer.valueOf(infoAboutTask[0]));
                newEpic.setStatus(Progress.valueOf(infoAboutTask[3]));
                newEpic.setDuration(Integer.parseInt(infoAboutTask[6]));
                newEpic.setStartTime(infoAboutTask[5]);
                newEpic.setEndTime(infoAboutTask[7]);
                return newEpic;
        }
        Subtask newSubtask = new Subtask(infoAboutTask[2], infoAboutTask[4], -1, -1,
                Integer.parseInt(infoAboutTask[7]), infoAboutTask[6]);
        newSubtask.setId(Integer.valueOf(infoAboutTask[0]));
        newSubtask.setStatus(Progress.valueOf(infoAboutTask[3]));
        newSubtask.setIdOfSubtaskEpic(Integer.parseInt(infoAboutTask[5]));
        return newSubtask;
    }

    private String getIdsOfViewedTasks() {
        String listIds = inMemoryTaskManager.getListOfHistory().stream()
                .map(Task::getId)
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        return "[" + listIds + "]";
    }

    //очистка файла-хранилища
    public void clearFile() {
        try {
            Files.delete(fileForSaving);
        } catch (IOException e) {
            throw new ManagerSaveException("Приносим свои извинения, вы не должны были видеть это!" +
                    "Произошла ошибка обновления хранилища: старые данные не могут быть удалены...");
        }

        fileForSaving = defaultPath;

        try {
            Files.createFile(fileForSaving);
        } catch (IOException e) {
            throw new ManagerSaveException("Приносим свои извинения, вы не должны были видеть это!" +
                    "Произошла ошибка обновления хранилища: не удалось создать новое хранилище");
        }

        writeHeadLine();
    }

    //обновление файла-хранилища
    private void recodeInFileFromArrays() {
        try (Writer writeNewStringInStorage = new FileWriter(fileForSaving.toString(),
                StandardCharsets.UTF_8, true)) {

            for (Task curTask : allTasks.values()) {
                writeNewStringInStorage.write(fromTaskToString(curTask) + "\n");
            }

            for (Epic curTask : allEpics.values()) {
                writeNewStringInStorage.write(fromTaskToString(curTask) + "\n");
            }

            for (Subtask curTask : allSubtasks.values()) {
                writeNewStringInStorage.write(fromTaskToString(curTask) + "\n");
            }

            writeNewStringInStorage.write(getIdsOfViewedTasks());
        } catch (IOException e) {
            throw new ManagerSaveException("Приносим свои извинения, вы не должны были видеть это!" +
                    "Произошла ошибка сохранения: новые данные не могут записаться...");
        }
    }

    private void recodeInArraysFromFile() {
        try (Reader reader = new FileReader(fileForSaving.toString());
             BufferedReader br = new BufferedReader(reader)) {
            String curTaskInString = br.readLine(); //чтобы проскочить первую строку "id,type,name,status,desc,epic\n"
            String list = "";
            while (br.ready()) {
                curTaskInString = br.readLine();

                if (curTaskInString.startsWith("[")) {
                    list = curTaskInString;
                    break;
                }

                String type = curTaskInString.split(",")[1];
                if (Tasks.valueOf(type) == Tasks.TASK) {
                    Task curTaskInTask = fromStringToTask(curTaskInString);
                    allTasks.put(curTaskInTask.getId(), curTaskInTask);
                    inMemoryTaskManager.sortedTasks.add(curTaskInTask);
                    inMemoryTaskManager.allTasks.put(curTaskInTask.getId(), curTaskInTask);
                } else if (Tasks.valueOf(type) == Tasks.EPIC) {
                    Epic curEpicInEpic = (Epic) fromStringToTask(curTaskInString);
                    allEpics.put(curEpicInEpic.getId(), curEpicInEpic);
                    inMemoryTaskManager.allEpics.put(curEpicInEpic.getId(), curEpicInEpic);
                } else {
                    Subtask curSubtaskInSubtask = (Subtask) fromStringToTask(curTaskInString);
                    allSubtasks.put(curSubtaskInSubtask.getId(), curSubtaskInSubtask);
                    inMemoryTaskManager.sortedTasks.add(curSubtaskInSubtask);
                    inMemoryTaskManager.allSubtasks.put(curSubtaskInSubtask.getId(), curSubtaskInSubtask);
                }
            }

            //но сейчас эпики не знают какие подзадачи к ним относятся
            for (Subtask subtask : allSubtasks.values()) {
                allEpics.get(subtask.getIdOfSubtaskEpic()).saveNewSubtaskIDs(subtask.getId());
            }

            inMemoryTaskManager.inMemoryHistoryManager.setFm(this);
            if (list.isEmpty()) {
                return;
            }

            String idsOfViewedTasks = list.substring(1, list.length() - 1);
            for (String id : idsOfViewedTasks.split(",")) {
                if (inMemoryTaskManager.isTaskAddedByID(Integer.parseInt(id))) {
                    inMemoryTaskManager.inMemoryHistoryManager.recordFromFBM(allTasks.get(Integer.parseInt(id)));
                } else if (inMemoryTaskManager.isEpicAddedByID(Integer.parseInt(id))) {
                    inMemoryTaskManager.inMemoryHistoryManager.recordFromFBM(allEpics.get(Integer.parseInt(id)));
                } else if (inMemoryTaskManager.isSubtaskAddedByID(Integer.parseInt(id))) {
                    inMemoryTaskManager.inMemoryHistoryManager.recordFromFBM(allSubtasks.get(Integer.parseInt(id)));
                }
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Приносим свои извинения, вы не должны были видеть это!" +
                    "Произошла ошибка обновления списка хранилища при загрузке из файла...");
        }
    }

    private void writeHeadLine() {
        try (Writer writeNewStringInStorage = new FileWriter(fileForSaving.toString(),
                StandardCharsets.UTF_8, true)) {
            writeNewStringInStorage.write(headLine);
        } catch (IOException e) {
            throw new ManagerSaveException("Приносим свои извинения, вы не должны были видеть это!" +
                    "Произошла ошибка сохранения при записи заголовка в хранилище...");
        }
    }

    @Override
    public String getPrioritizedTasks() {
        return inMemoryTaskManager.getPrioritizedTasks();
    }

    //автосохранение
    private void save() {
        clearFile();
        recodeInFileFromArrays();
    }

    //методы с автосохранением
    public void needSave() {
        save();
    }

    //удалить что-то
    @Override
    public void removeAllTasks() {
        super.removeAllTasks();
        save();
    }

    @Override
    public void removeAllEpics() {
        super.removeAllEpics();
        save();
    }


    @Override
    public void deleteOneTaskByID(int idForDelete) {
        super.deleteOneTaskByID(idForDelete);
        save();
    }

    @Override
    public void deleteOneEpicByID(int idForDelete) {
        super.deleteOneEpicByID(idForDelete);
        save();
    }

    @Override
    public void deleteOneSubtaskskByID(int idForDelete) {
        super.deleteOneSubtaskskByID(idForDelete);
        save();
    }

    //сохранить что-то
    @Override
    public Task saveNewTask(Task task) {
        super.saveNewTask(task);
        save();
        return task;
    }

    @Override
    public Epic saveNewEpic(Epic epic) {
        allEpics.put(epic.getId(), epic);
        save();
        return epic;
    }

    @Override
    public Subtask saveNewSubtask(Subtask subtask) {
        allSubtasks.put(subtask.getId(), subtask);
        save();
        return subtask;
    }

    //обновить что-то
    @Override
    public Task updateTask(Task task) {
        super.updateTask(task);
        save();
        return task;
    }

    @Override
    public Epic updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
        return epic;
    }

    @Override
    public Subtask updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
        return subtask;
    }
}