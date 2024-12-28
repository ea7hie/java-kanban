package com.yandex.app.service;

import com.yandex.app.model.*;
import com.yandex.app.service.exceptions.ManagerSaveException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private Path fileForSaving;
    private final HashMap<Integer, Task> allTasks = new HashMap<>();
    private final HashMap<Integer, Epic> allEpics = new HashMap<>();
    private final HashMap<Integer, Subtask> allSubtasks = new HashMap<>();

    //создатели
    public FileBackedTaskManager(Path fileForSaving) {
        this.fileForSaving = fileForSaving;
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        return new FileBackedTaskManager(file.toPath());
    }

    //геттеры
    public HashMap<Integer, Task> getAllTasksInMap() {
        return allTasks;
    }

    public HashMap<Integer, Epic> getAllEpicsInMap() {
        return allEpics;
    }

    public HashMap<Integer, Subtask> getAllSubtasksInMap() {
        return allSubtasks;
    }

    //конвертеры
    private String fromTaskToString(Task task) {
        int indexOfType = task.getClass().toString().split("\\.").length - 1;
        String type = task.getClass().toString().split("\\.")[indexOfType].toUpperCase().toUpperCase();

        if (task.getClass().toString().equals("class com.yandex.app.model.Subtask")) {
            Subtask subtask = (Subtask) task;
            String[] infoAboutTask = {String.valueOf(subtask.getId()),
                    Tasks.valueOf(type).toString(), subtask.getName(),
                    subtask.getStatus().toString(), subtask.getDescription(),
                    String.valueOf(subtask.getIdOfSubtaskEpic())
            };
            return String.join(",", infoAboutTask);
        }

        String[] infoAboutTask = {String.valueOf(task.getId()),
                Tasks.valueOf(type).toString(),
                task.getName(), task.getStatus().toString(), task.getDescription()
        };
        return String.join(",", infoAboutTask);
    }

    private Task fromStringToTask(String taskInString) {
        String[] infoAboutTask = taskInString.split(",");

        switch (infoAboutTask[1]) {
            case "TASK":
                Task newTask = new Task(infoAboutTask[2], infoAboutTask[4], -1);
                newTask.setId(Integer.valueOf(infoAboutTask[0]));
                newTask.setStatus(Progress.valueOf(infoAboutTask[3]));
                return newTask;
            case "EPIC":
                Epic newEpic = new Epic(infoAboutTask[2], infoAboutTask[4], -1);
                newEpic.setId(Integer.valueOf(infoAboutTask[0]));
                newEpic.setStatus(Progress.valueOf(infoAboutTask[3]));
                return newEpic;
        }
        Subtask newSubtask = new Subtask(infoAboutTask[2], infoAboutTask[4], -1, -1);
        newSubtask.setId(Integer.valueOf(infoAboutTask[0]));
        newSubtask.setStatus(Progress.valueOf(infoAboutTask[3]));
        newSubtask.setIdOfSubtaskEpic(Integer.parseInt(infoAboutTask[5]));
        return newSubtask;
    }

    //очистка файла-хранилища
    private void clearFile() {
        try {
            Files.delete(fileForSaving);
        } catch (IOException e) {
            throw new ManagerSaveException("Приносим свои извинения, вы не должны были видеть это!" +
                    "Произошла ошибка обновления хранилища: старые данные не могут быть удалены...");
        }

        fileForSaving = Paths.get("C:\\Users\\user\\IdeaProjects\\4sprintBeginningFinalTask\\java-kanban" +
                "\\src\\com\\yandex\\app\\service\\storage\\allTasks.txt");

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
        } catch (IOException e) {
            throw new ManagerSaveException("Приносим свои извинения, вы не должны были видеть это!" +
                    "Произошла ошибка сохранения: новые данные не могут записаться...");
        }
    }

    public void recodeInArraysFromFile() {
        try (Reader reader = new FileReader(fileForSaving.toString());
             BufferedReader br = new BufferedReader(reader)) {
            String curTask = br.readLine(); //чтобы проскочить первую строку "id,type,name,status,description,epic\n"
            while (br.ready()) {
                curTask = br.readLine();
                if (curTask.split(",")[1].equals("TASK")) {
                    allTasks.put(Integer.parseInt(curTask.split(",")[0]), fromStringToTask(curTask));
                } else if (curTask.split(",")[1].equals("EPIC")) {
                    allEpics.put(Integer.parseInt(curTask.split(",")[0]), (Epic) fromStringToTask(curTask));
                } else {
                    allSubtasks.put(Integer.parseInt(curTask.split(",")[0]),
                            (Subtask) fromStringToTask(curTask));
                }
            }

            //но теперь эпики не знают какие подзадачи к ним относятся
            for (Subtask subtask : allSubtasks.values()) {
                allEpics.get(subtask.getIdOfSubtaskEpic()).saveNewSubtaskIDs(subtask.getId());
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Приносим свои извинения, вы не должны были видеть это!" +
                    "Произошла ошибка обновления списка хранилища при загрузке из файла...");
        }
    }

    private void writeHeadLine() {
        try (Writer writeNewStringInStorage = new FileWriter(fileForSaving.toString(),
                StandardCharsets.UTF_8, true)) {
            writeNewStringInStorage.write("id,type,name,status,description,epic\n");
        } catch (IOException e) {
            throw new ManagerSaveException("Приносим свои извинения, вы не должны были видеть это!" +
                    "Произошла ошибка сохранения при записи заголовка в хранилище...");
        }
    }

    //автосохранение
    private void save() {
        clearFile();
        recodeInFileFromArrays();
    }

    //методы с автосохранением

    //удалить что-то
    @Override
    public void removeAllTasks() {
        allTasks.clear();
        save();
    }

    @Override
    public void removeAllEpics() {
        allEpics.clear();
        allSubtasks.clear();
        save();
    }


    @Override
    public void deleteOneTaskByID(int idForDelete) {
        allTasks.remove(idForDelete);
        save();
    }

    @Override
    public void deleteOneEpicByID(int idForDelete) {
        for (int index : allEpics.get(idForDelete).getSubtasksIDs()) {
            allSubtasks.remove(index);
        }
        allEpics.remove(idForDelete);
        save();
    }

    @Override
    public void deleteOneSubtaskskByID(int idForDelete) {
        int idOfEpic = allSubtasks.get(idForDelete).getIdOfSubtaskEpic();
        allEpics.get(idOfEpic).getSubtasksIDs().remove(Integer.valueOf(idForDelete));
        allSubtasks.remove(idForDelete);
        checkProgressStatusOfEpic(idOfEpic);
        save();
    }

    //сохранить что-то
    @Override
    public Task saveNewTask(Task task) {
        allTasks.put(task.getId(), task);
        save();
        return task;
    }

    @Override
    public Epic saveNewEpic(Epic epic) {
        if (epic.getSubtasksIDs() != null) {
            for (int subtaskID : epic.getSubtasksIDs()) {
                allSubtasks.get(subtaskID).setIdOfSubtaskEpic(epic.getId());
            }
        }
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
        allTasks.put(task.getId(), task);
        save();
        return task;
    }

    @Override
    public Epic updateEpic(Epic epic) {
        allEpics.put(epic.getId(), epic);
        save();
        return epic;
    }

    @Override
    public Subtask updateSubtask(Subtask subtask) {
        allSubtasks.put(subtask.getId(), subtask);
        checkProgressStatusOfEpic(subtask.getIdOfSubtaskEpic());
        save();
        return subtask;
    }

    private void checkProgressStatusOfEpic(int idOfEpic) {
        int counterOfNEW = 0;
        int counterOfDONE = 0;

        Epic checkedEpic = allEpics.get(idOfEpic);
        for (Integer id : checkedEpic.getSubtasksIDs()) {
            Subtask currentCheckingSubtask = allSubtasks.get(id);
            if (currentCheckingSubtask.getStatus().equals(Progress.NEW)) {
                counterOfNEW++;
            } else if (currentCheckingSubtask.getStatus().equals(Progress.DONE)) {
                counterOfDONE++;
            }
        }

        if (counterOfNEW == checkedEpic.getSubtasksIDs().size()) {
            checkedEpic.setStatus(Progress.NEW);
        } else if (counterOfDONE == checkedEpic.getSubtasksIDs().size()) {
            checkedEpic.setStatus(Progress.DONE);
        } else {
            checkedEpic.setStatus(Progress.IN_PROGRESS);
        }
    }
}