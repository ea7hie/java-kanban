package com.yandex.app.service;

import com.yandex.app.model.*;
import com.yandex.app.service.exceptions.ManagerSaveException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private Path fileForSaving;
    private final Path defaultPath;

    //создатели
    public FileBackedTaskManager(Path fileForSaving) {
        this.fileForSaving = fileForSaving;
        this.defaultPath = fileForSaving;
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager fm = new FileBackedTaskManager(file.toPath());
        fm.recodeInArraysFromFile();
        return fm;
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

        switch (Tasks.valueOf(infoAboutTask[1])) {
            case Tasks.TASK:
                Task newTask = new Task(infoAboutTask[2], infoAboutTask[4], -1);
                newTask.setId(Integer.valueOf(infoAboutTask[0]));
                newTask.setStatus(Progress.valueOf(infoAboutTask[3]));
                return newTask;
            case Tasks.EPIC:
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
        } catch (IOException e) {
            throw new ManagerSaveException("Приносим свои извинения, вы не должны были видеть это!" +
                    "Произошла ошибка сохранения: новые данные не могут записаться...");
        }
    }

    private void recodeInArraysFromFile() {
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

            //но сейчас эпики не знают какие подзадачи к ним относятся
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
        super.saveNewEpic(epic);
        save();
        return epic;
    }

    @Override
    public Subtask saveNewSubtask(Subtask subtask) {
        super.saveNewSubtask(subtask);
        allEpics.get(subtask.getIdOfSubtaskEpic()).saveNewSubtaskIDs(subtask.getId());
        checkProgressStatusOfEpic(subtask.getIdOfSubtaskEpic());
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