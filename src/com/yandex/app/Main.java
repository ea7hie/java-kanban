package com.yandex.app;

import com.yandex.app.model.Epic;
import com.yandex.app.model.Progress;
import com.yandex.app.model.Subtask;
import com.yandex.app.model.Task;
import com.yandex.app.service.InMemoryHistoryManager;
import com.yandex.app.service.InMemoryTaskManager;
import com.yandex.app.service.Managers;

import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    public static Scanner scanner = new Scanner(System.in);
    public static InMemoryTaskManager inMemoryTaskManager = new InMemoryTaskManager();
    public static InMemoryHistoryManager inMemoryHistoryManager = (InMemoryHistoryManager) Managers.getDefaultHistory();

    public static void main(String[] args) {
        String[] enumsProgress = {"NEW", "IN_PROGRESS", "DONE"};

        inMemoryTaskManager.saveNewTask(new Task("!!!1", "desc 1", 1));
        inMemoryTaskManager.saveNewTask(new Task("!!!2", "desc 2", 1));
        inMemoryTaskManager.saveNewTask(new Task("!!!3", "desc 3", 1));

        inMemoryTaskManager.saveNewEpic(new Epic("???1", "DESC 1", 1));
        inMemoryTaskManager.saveNewEpic(new Epic("???2", "DESC 2", 1));
        inMemoryTaskManager.saveNewEpic(new Epic("???3", "DESC 3", 1));

        inMemoryTaskManager.saveNewSubtask(new Subtask("---1", "Desc---4", 1, 4));
        inMemoryTaskManager.saveNewSubtask(new Subtask("---2", "Desc---4", 1, 4));
        inMemoryTaskManager.saveNewSubtask(new Subtask("---3", "Desc---4", 1, 4));
        inMemoryTaskManager.saveNewSubtask(new Subtask("---4", "Desc---4", 1, 4));

        inMemoryTaskManager.saveNewSubtask(new Subtask("---1", "Desc---5", 1, 5));
        inMemoryTaskManager.saveNewSubtask(new Subtask("---2", "Desc---5", 1, 5));
        inMemoryTaskManager.saveNewSubtask(new Subtask("---3", "Desc---5", 1, 5));
        inMemoryTaskManager.saveNewSubtask(new Subtask("---4", "Desc---5", 1, 5));


        System.out.println("Добро пожаловать в Трекер Задач, в ваш персональный помощник!\n");
        String typeOfTask;
        while (true) {
            printMenuOfTypes();
            do {
                System.out.println(
                        "\nВведите нужную команду соответствующей цифрой: \n" +
                                "работа с приложением: 0(выход) или 3(просмотр недавних), \n" +
                                "работа с задачами: 1(задача) или 2(эпик)."
                );
                typeOfTask = scanner.next();
            } while (!typeOfTask.equals("1") && !typeOfTask.equals("2") && !typeOfTask.equals("0")
                    && !typeOfTask.equals("3"));

            if (typeOfTask.equals("0")) {
                return;
            }

            if (typeOfTask.equals("3")) {
                System.out.println(showListViewedTasks());
                continue;
            }

            boolean isTask = typeOfTask.equals("1");

            printMenuForTask();
            if (!isTask) {
                printMenuForEpic();
            }

            String cmd = scanner.next();//выбор пункта меня "что ты хочешь сделать"
            String name;
            String description;
            int temporaryID = -1;
            boolean isElementByIdSaved;

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
                        inMemoryTaskManager.removeAllTasks();
                        inMemoryHistoryManager.removeAllTasksInViewedTasks();
                    } else {
                        inMemoryTaskManager.removeAllEpics();
                        inMemoryHistoryManager.removeAllEpicsInViewedTasks();
                    }
                    break;
                case "3":
                    System.out.println("Введите id задачи, которую хотите найти.");
                    int idForSearch = checkNextInt();

                    if (inMemoryTaskManager.isTaskAddedByID(idForSearch)
                            || inMemoryTaskManager.isEpicAddedByID(idForSearch)
                            || inMemoryTaskManager.isSubtaskAddedByID(idForSearch)) {
                        if (isTask) {
                            System.out.println(inMemoryTaskManager.findTaskByID(idForSearch));
                            inMemoryHistoryManager.add(inMemoryTaskManager.findTaskByID(idForSearch));

                        } else if (inMemoryTaskManager.isEpicAddedByID(idForSearch)) {
                            System.out.println(inMemoryTaskManager.findEpicByID(idForSearch));
                            inMemoryHistoryManager.add(inMemoryTaskManager.findEpicByID(idForSearch));
                        } else {
                            System.out.println(inMemoryTaskManager.findSubtaskByID(idForSearch));
                            inMemoryHistoryManager.add(inMemoryTaskManager.findSubtaskByID(idForSearch));
                        }
                    } else {
                        System.out.println("Задачи с таким id нет в вашем списке.");
                    }
                    break;
                case "4":
                    name = inputNameOfTask();
                    description = inputDescriptionOfTask();

                    if (isTask) {
                        inMemoryTaskManager.saveNewTask(new Task(name, description, temporaryID));
                    } else {
                        System.out.println("Сколько шагов до вашей цели?");
                        int amountSteps = checkNextInt();

                        Epic newEpic = new Epic(name, description, temporaryID);
                        inMemoryTaskManager.saveNewEpic(newEpic);
                        for (int i = 1; i <= amountSteps; i++) {
                            Subtask newSubtask = new Subtask(
                                    inputNameSubtaskOfEpic(), inputDescriptionSubtaskOfEpic(),
                                    temporaryID, newEpic.getId()
                            );
                            inMemoryTaskManager.saveNewSubtask(newSubtask);
                        }
                    }
                    System.out.println("Успешно сохранено!");
                    break;
                case "5":
                    System.out.println("Введите id элемента, который хотите изменить");
                    int idForUpdate = checkNextInt();

                    isElementByIdSaved = inMemoryTaskManager.isTaskAddedByID(idForUpdate)
                            || inMemoryTaskManager.isEpicAddedByID(idForUpdate);

                    if (isElementByIdSaved) {
                        printMenuForUpdate();
                        int choosingWhatToUpdate = checkNextInt();

                        switch (choosingWhatToUpdate) {
                            case 1:
                                if (isTask) {
                                    Task newTask = new Task(
                                            getNewValueForUpdate(),
                                            inMemoryTaskManager.findTaskByID(idForUpdate).getDescription(),
                                            idForUpdate
                                    );
                                    inMemoryTaskManager.updateTask(newTask);
                                    inMemoryHistoryManager.updateOneElem(newTask);
                                } else {
                                    Epic newEpic =  new Epic(
                                            getNewValueForUpdate(),
                                            inMemoryTaskManager.findEpicByID(idForUpdate).getDescription(),
                                            idForUpdate
                                    );
                                    inMemoryTaskManager.updateEpic(newEpic);
                                    inMemoryHistoryManager.updateOneElem(newEpic);
                                }
                                break;
                            case 2:
                                if (isTask) {
                                    Task newTask =  new Task(
                                            inMemoryTaskManager.findTaskByID(idForUpdate).getName(),
                                            getNewValueForUpdate(), idForUpdate
                                    );
                                    inMemoryTaskManager.updateTask(newTask);
                                    inMemoryHistoryManager.updateOneElem(newTask);
                                } else {
                                    Epic newEpic =  new Epic(
                                            inMemoryTaskManager.findEpicByID(idForUpdate).getName(),
                                            getNewValueForUpdate(), idForUpdate
                                    );
                                    inMemoryTaskManager.updateEpic(newEpic);
                                    inMemoryHistoryManager.updateOneElem(newEpic);
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
                                                inMemoryTaskManager.findTaskByID(idForUpdate).getName(),
                                                inMemoryTaskManager.findTaskByID(idForUpdate).getDescription(),
                                                idForUpdate
                                        );
                                        newTask.setStatus(newStatus);
                                        inMemoryTaskManager.updateTask(newTask);
                                        inMemoryHistoryManager.updateOneElem(newTask);
                                    } else {
                                        System.out.println("Введите id подзадачи, которую нужно сменить.");
                                        int index = checkNextInt();
                                        Epic epic = inMemoryTaskManager.findEpicByID(idForUpdate);
                                        ArrayList<Integer> indexes = epic.getSubtasksIDs();

                                        if (indexes.contains(index)) {
                                            Subtask newSubtask = new Subtask(
                                                    inMemoryTaskManager.findSubtaskByID(index).getName(),
                                                    inMemoryTaskManager.findSubtaskByID(index).getDescription(),
                                                    index, epic.getId()
                                            );
                                            newSubtask.setStatus(newStatus);
                                            inMemoryTaskManager.updateSubtask(newSubtask);
                                            inMemoryHistoryManager.updateOneElem(newSubtask);
                                        } else {
                                            System.out.println("Ошибка. Подзадачи с этим номером нет.");
                                        }
                                        System.out.println("Успешно сохранено!\n");
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
                                    System.out.println("Введите новые значения.");

                                    Epic epic = inMemoryTaskManager.findEpicByID(idForUpdate);
                                    ArrayList<Integer> indexes = epic.getSubtasksIDs();

                                    if (indexes.contains(index)) {
                                        Subtask newSubtask = new Subtask(inputNameSubtaskOfEpic(),
                                                inputDescriptionSubtaskOfEpic(), index, idForUpdate
                                        );
                                        inMemoryTaskManager.updateSubtask(newSubtask);
                                        inMemoryHistoryManager.updateOneElem(newSubtask);
                                    } else {
                                        System.out.println("Ошибка. Подзадачи с этим id в этом эпике нет.");
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
                    int idForDelete = checkNextInt();
                    isElementByIdSaved = inMemoryTaskManager.isTaskAddedByID(idForDelete)
                            || inMemoryTaskManager.isEpicAddedByID(idForDelete)
                            || inMemoryTaskManager.isSubtaskAddedByID(idForDelete);
                    if (isElementByIdSaved) {
                        System.out.println(inMemoryTaskManager.deleteOneElementByID(idForDelete));
                        inMemoryHistoryManager.removeOneElem(idForDelete);
                    } else {
                        System.out.println("Задачи с таким id нет в вашем списке.");
                    }
                    break;
                case "7":
                    System.out.println(showListViewedTasks());
                    break;
                case "8":
                    System.out.println("Введите id эпика, который хотите посмотреть.");
                    int idForViewEpic = checkNextInt();
                    if (inMemoryTaskManager.isEpicAddedByID(idForViewEpic)) {
                        ArrayList<Subtask> subtasksForView = inMemoryTaskManager.getAllSubtasksOfEpicById(idForViewEpic);
                        for (int i = 0; i < subtasksForView.size(); i++) {
                            Subtask currentSubtask = subtasksForView.get(i);
                            System.out.println(
                                    "Подзадача №" + (i + 1) + " '" + currentSubtask.getName() + "', "
                                    + "описание: " + currentSubtask.getDescription() +  ", "
                                    + "id=" + currentSubtask.getId() + ", "
                                    + "статус прогресса: " + currentSubtask.getStatus()
                            );
                        }
                        System.out.println();
                        inMemoryHistoryManager.add(inMemoryTaskManager.findEpicByID(idForViewEpic));
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
        System.out.println("С каким видом задачи хотите работать?");
        System.out.println("1 - Задача (одно действие для достижения результа),");
        System.out.println("2 - Эпик (несколько действий для достижения результа).");
        System.out.println("3 - Просмотр недавних.");
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
        System.out.println("7 - посмотреть список последних 10 просмотренных задач");
    }

    public static void printMenuForEpic() {
        System.out.println("8 - посмотреть все задачи эпика по его id");
    }

    public static void printMenuForUpdate() {
        System.out.println();
        System.out.println("Что вы хотите обновить? Введите нужную команду:");
        System.out.println("1 - название");
        System.out.println("2 - описание");
        System.out.println("3 - статус прогресса");
        System.out.println("4 - изменить подзадачу (для эпиков)");
    }

    public static void printMenuForProgresses() {
        System.out.println("1 - NEW");
        System.out.println("2 - IN_PROGRESS");
        System.out.println("3 - DONE");
    }

    public static String printAllTasks() {
        if (inMemoryTaskManager.getAllTasks().isEmpty()) {
            return "Список задач пуст.\n";
        } else {
            String messageWithAllTasks = "";
            for (Task task : inMemoryTaskManager.getAllTasks()) {
                messageWithAllTasks = messageWithAllTasks + task.toString();
            }
            return messageWithAllTasks;
        }
    }

    public static String printAllEpics() {
        if (inMemoryTaskManager.getAllEpics().isEmpty()) {
            return "Список эпиков пуст.\n";
        } else {
            String messageWithAllEpics = "";
            for (Epic epic : inMemoryTaskManager.getAllEpics()) {
                messageWithAllEpics = messageWithAllEpics + epic.toString();
            }
            return messageWithAllEpics;
        }
    }

    public static String showListViewedTasks() {
        ArrayList<Task> viewedTasks = inMemoryHistoryManager.getHistory();
        if (viewedTasks.isEmpty()) {
            return "Пока что нет недавно просмотренных задач.\n";
        }

        String listViewedTasks = "";
        for (int i = 0; i < viewedTasks.size(); i++) {
            Task viewedTask = viewedTasks.get(i);
            listViewedTasks = listViewedTasks + (i + 1) + "-я просмотренная задача: \n" + viewedTask.toString() + "\n";
        }
        return listViewedTasks;
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

    public static String inputNameSubtaskOfEpic() {
        System.out.println("Введите название нового шага к вашей цели.");
        return scanner.nextLine();
    }

    public static String inputDescriptionSubtaskOfEpic() {
        System.out.println("Введите описание нового шага к вашей цели.");
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