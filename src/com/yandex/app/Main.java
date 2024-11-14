package com.yandex.app;

import com.yandex.app.model.Epic;
import com.yandex.app.model.Progress;
import com.yandex.app.model.Subtask;
import com.yandex.app.model.Task;
import com.yandex.app.service.TaskManager;

import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    public static Scanner scanner = new Scanner(System.in);
    public static TaskManager taskManager = new TaskManager();

    public static void main(String[] args) {
        String[] enumsProgress = {"NEW", "IN_PROGRESS", "DONE"};

        System.out.println("Добро пожаловать в Трекер Задач, в ваш персональный помощник!");
        String typeOfTask;
        while (true) {
            printMenuOfTypes();

            do {
                System.out.println("Введите нужную команду соответствующей цифрой: 0(выход), 1(задача) или 2(эпик).");
                typeOfTask = scanner.next();
            } while (!typeOfTask.equals("1") && !typeOfTask.equals("2") && !typeOfTask.equals("0"));

            boolean isTask = typeOfTask.equals("1");

            if (typeOfTask.equals("0")) {
                return;
            }

            printMenuForTask();
            if (!isTask) {
                printMenuForEpic();
            }

            String cmd = scanner.next();//выбор пункта меня "что ты хочешь сделать"

            String name;
            String description;
            ArrayList<Subtask> subtasks = new ArrayList<>();

            switch (cmd) {
                case "0":
                    return;
                case "1":
                    if (isTask) {
                        System.out.println(printAllTasks());
                    } else {
                        System.out.println(printAllEpics());
                    }
                    break;
                case "2":
                    if (isTask) {
                        taskManager.removeAllTasks();
                    } else {
                        taskManager.removeAllEpics();
                    }
                    break;
                case "3":
                    System.out.println("Введите id задачи, которую хотите найти.");
                    int idForSearch = checkNextInt();

                    if (taskManager.findTaskByID(idForSearch) != null
                            || taskManager.findEpicByID(idForSearch) != null) {
                        if (isTask) {
                            System.out.println(taskManager.findTaskByID(idForSearch));
                        } else {
                            System.out.println(taskManager.findEpicByID(idForSearch));
                        }
                    } else {
                        System.out.println("Задачи с таким id нет в вашем списке.");
                    }
                    break;
                case "4":
                    name = inputNameOfTask();
                    description = inputDescriptionOfTask();

                    if (isTask) {
                        taskManager.saveNewTask(new Task(name, description, taskManager.generateNewID()));
                    } else {
                        System.out.println("Сколько шагов до вашей цели?");
                        int amountSteps = checkNextInt();

                        int idNewEpic = taskManager.generateNewID();
                        Epic newEpic = new Epic(name, description, idNewEpic);

                        for (int i = 1; i <= amountSteps; i++) {
                            Subtask newSubtask = new Subtask(
                                    inputSubtaskOfEpic(), taskManager.generateNewID(), idNewEpic
                            );
                            subtasks.add(newSubtask);
                            newEpic.saveNewSubtaskIDs(newSubtask.getId());
                        }

                        taskManager.saveNewEpic(newEpic);
                        taskManager.saveNewSubtask(idNewEpic, subtasks);
                    }
                    System.out.println("Успешно сохранено!");
                    break;
                case "5":
                    System.out.println("Введите id элемента, который хотите изменить");
                    int idForUpdate = checkNextInt();

                    boolean isElementByIdSaved = taskManager.isTaskAddedByID(idForUpdate)
                            || taskManager.isEpicAddedByID(idForUpdate);

                    if (isElementByIdSaved) {
                        printMenuForUpdate();
                        int choosingWhatToUpdate = checkNextInt();

                        switch (choosingWhatToUpdate) {
                            case 1:
                                if (isTask) {
                                    taskManager.updateTask(
                                            new Task(
                                                    getNewValueForUpdate(),
                                                    taskManager.findTaskByID(idForUpdate).getDescription(),
                                                    idForUpdate
                                            )
                                    );
                                } else {
                                    taskManager.updateEpic(
                                            new Epic(
                                                    getNewValueForUpdate(),
                                                    taskManager.findEpicByID(idForUpdate).getDescription(),
                                                    idForUpdate
                                            )
                                    );
                                }
                                break;
                            case 2:
                                if (isTask) {
                                    taskManager.updateTask(
                                            new Task(
                                                    taskManager.findTaskByID(idForUpdate).getName(),
                                                    getNewValueForUpdate(), idForUpdate
                                            )
                                    );
                                } else {
                                    taskManager.updateEpic(
                                            new Epic(
                                                    taskManager.findEpicByID(idForUpdate).getName(),
                                                    getNewValueForUpdate(), idForUpdate
                                            )
                                    );
                                }
                                break;
                            case 3:
                                printMenuForProgresses();
                                System.out.println("Введите новое значение.");
                                int indexOfNewProgStatus = checkNextInt();

                                if (indexOfNewProgStatus >= 1 && indexOfNewProgStatus <= 3) {
                                    Progress newStatus = Progress.valueOf(enumsProgress[indexOfNewProgStatus - 1]);

                                    if (isTask) {
                                        Task newTask = new Task(
                                                taskManager.findTaskByID(idForUpdate).getName(),
                                                taskManager.findTaskByID(idForUpdate).getDescription(),
                                                idForUpdate
                                        );
                                        newTask.setStatus(newStatus);
                                        taskManager.updateTask(newTask);
                                    } else {
                                        System.out.println("Введите id подзадачи, которую нужно сменить.");
                                        int index = checkNextInt();
                                        Epic epic = taskManager.findEpicByID(idForUpdate);
                                        ArrayList<Integer> indexes = epic.getSubtasksIDs();

                                        if (indexes.contains(index)) {
                                            taskManager.findSubtaskByID(index, idForUpdate).setStatus(newStatus);

                                            taskManager.findEpicByID(idForUpdate).setStatus(
                                                    taskManager.checkProgressStatusOfEpic(idForUpdate)
                                            );
                                        } else {
                                            System.out.println("Ошибка. Подзадачи с этим номером нет.");
                                        }
                                        System.out.println("Успешно сохранено!");
                                    }
                                } else {
                                    System.out.println("Введите число от 1 до 3.");
                                }
                                break;
                            case 4:
                                if (isTask) {
                                    System.out.println("Данная команда предназначена только для эпиков.");
                                } else {
                                    System.out.println("Введите id подзадачи, которую нужно сменить.");
                                    int index = checkNextInt();
                                    System.out.println("Введите новое значение.");
                                    String newValue = scanner.nextLine();

                                    Epic epic = taskManager.findEpicByID(idForUpdate);
                                    ArrayList<Integer> indexes = epic.getSubtasksIDs();

                                    if (indexes.contains(index)) {
                                        taskManager.findSubtaskByID(index, idForUpdate).setName(newValue);
                                    } else {
                                        System.out.println("Ошибка. Подзадачи с этим номером нет.");
                                    }
                                }
                                break;
                            default:
                                System.out.println("Такой команды нет.");
                                break;
                        }
                    } else {
                        System.out.println("Не найдено задачи с таким id в вашем списке.");
                    }
                    break;
                case "6":
                    System.out.println("Введите id элемента, который хотите удалить.");
                    System.out.println(taskManager.deleteOneElementByID(checkNextInt()));
                    break;
                case "7":
                    System.out.println("Введите id эпика, который хотите посмотреть.");
                    int idForViewEpic = checkNextInt();
                    if (taskManager.isEpicAddedByID(idForViewEpic)) {
                        System.out.println(taskManager.showOneEpicByID(idForViewEpic));
                    } else {
                        System.out.println("Эпика с таким id не найдено.");
                    }
                    break;
                default:
                    System.out.println("А такой команды у нас ещё нет.");
                    break;
            }
        }
    }

    public static void printMenuOfTypes() {
        System.out.println();
        System.out.println("С каким видом задачи хотите работать?");
        System.out.println("1 - Задача (одно действие для достижения результа),");
        System.out.println("2 - Эпик (несколько действий для достижения результа).");
        System.out.println("0 - Выход из приложения.");
    }

    public static void printMenuForTask() {
        System.out.println();
        System.out.println("Что вы хотите сделать? Введите нужную команду:");
        System.out.println("0 - выйти из приложения");
        System.out.println("1 - посмотреть список всех задач");
        System.out.println("2 - удалить все задачи");
        System.out.println("3 - найти задачу по её id");
        System.out.println("4 - добавить новую задачу");
        System.out.println("5 - изменить задачу по её id");
        System.out.println("6 - удалить задачу по её id");
    }

    public static void printMenuForUpdate() {
        System.out.println();
        System.out.println("Что вы хотите обновить? Введите нужную команду:");
        System.out.println("1 - название");
        System.out.println("2 - описание");
        System.out.println("3 - статус прогресса");
        System.out.println("4 - изменить подзадачу (для эпиков)");
    }

    public static void printMenuForEpic() {
        System.out.println("7 - посмотреть все задачи эпика по его id");
    }

    public static void printMenuForProgresses() {
        System.out.println("1 - NEW");
        System.out.println("2 - IN_PROGRESS");
        System.out.println("3 - DONE");
    }

    public static String printAllTasks() {
        if (taskManager.getAllTasks().isEmpty()) {
            return "Список задач пуст.";
        } else {
            String messageWithAllTasks = "";
            for (Task task : taskManager.getAllTasks()) {
                messageWithAllTasks = messageWithAllTasks + task.toString();
            }
            return messageWithAllTasks;
        }
    }

    public static String printAllEpics() {
        if (taskManager.getAllEpics().isEmpty()) {
            return "Список эпиков пуст.";
        } else {
            String messageWithAllEpics = "";
            for (Epic epic : taskManager.getAllEpics()) {
                messageWithAllEpics = messageWithAllEpics + epic.toString();
            }
            return messageWithAllEpics;
        }
    }

    public static String inputNameOfTask() {
        scanner.nextLine();
        System.out.println("Введите название вашей задачи (конечный результат, цель)");
        return scanner.nextLine();
    }

    public static String inputDescriptionOfTask() {
        System.out.println("Введите описание вашей задачи (например, мотивацию)");
        return scanner.nextLine();
    }

    public static String inputSubtaskOfEpic() {
        System.out.println("Введите новый шаг к вашей цели.");
        return scanner.nextLine();
    }

    public static int checkNextInt() {
        int number;
        while (true) {
            if (scanner.hasNextInt()) {
                number = scanner.nextInt();
                scanner.nextLine();
                return number;
            }
            System.out.println("Введено не число. Повторите ввод.");
            scanner.nextLine();
        }
    }

    public static String getNewValueForUpdate() {
        System.out.println("Введите новое значение.");
        return scanner.nextLine();
    }
}
