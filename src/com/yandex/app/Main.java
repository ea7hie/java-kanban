package com.yandex.app;

import com.yandex.app.model.Epic;
import com.yandex.app.model.Progress;
import com.yandex.app.model.Subtask;
import com.yandex.app.model.Task;
import com.yandex.app.service.FileBackedTaskManager;
import com.yandex.app.service.InMemoryTaskManager;
import com.yandex.app.service.Managers;
import com.yandex.app.service.exceptions.ManagerSaveException;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    public static Scanner scanner = new Scanner(System.in);
    public static InMemoryTaskManager inMemoryTaskManager = (InMemoryTaskManager) Managers.getDefaultTaskManager();

    public static void main(String[] args) {
        String[] enumsProgress = {"NEW", "IN_PROGRESS", "DONE"};

        Path dirForSave = Paths.get("C:\\Users\\user\\IdeaProjects\\4sprintBeginningFinalTask\\java-kanban" +
                "\\src\\com\\yandex\\app\\service\\storage");

        Path fileForSave = Paths.get("C:\\Users\\user\\IdeaProjects\\4sprintBeginningFinalTask\\java-kanban" +
                "\\src\\com\\yandex\\app\\service\\storage\\allTasks.txt");

        FileBackedTaskManager fm;
        if (!Files.exists(dirForSave)) {
            try {
                Files.createDirectory(dirForSave);
                Files.createFile(fileForSave);
                try (Writer writeNewStringInStorage = new FileWriter(fileForSave.toString(),
                        StandardCharsets.UTF_8, true)) {
                    writeNewStringInStorage.write("id,type,name,status,description,epic\n");
                } catch (IOException e) {
                    throw new ManagerSaveException("Приносим свои извинения, вы не должны были видеть это!" +
                            "Произошла ошибка сохранения...");
                }
            } catch (IOException e) {
                throw new ManagerSaveException("Приносим свои извинения, вы не должны были видеть это!" +
                        "Произошла ошибка создания хранилища...");
            }
            fm = new FileBackedTaskManager(fileForSave);
        } else {
            fm = FileBackedTaskManager.loadFromFile(fileForSave.toFile());

            for (Task task : fm.getAllTasks()) {
                inMemoryTaskManager.addTaskFromFile(task);
            }

            for (Epic epic : fm.getAllEpics()) {
                inMemoryTaskManager.addEpicFromFile(epic);
            }

            for (Subtask subtask : fm.getAllSubtasks()) {
                inMemoryTaskManager.addSubtaskFromFile(subtask);
            }

            inMemoryTaskManager.setIdOfNewTask(inMemoryTaskManager.getAllTasks().size() +
                    inMemoryTaskManager.getAllEpics().size() + inMemoryTaskManager.getAllSubtasks().size());
        }

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
                System.out.println(inMemoryTaskManager.showListViewedTasks());
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
                        fm.removeAllTasks();
                        System.out.println("Список задач очищен.\n");
                    } else {
                        inMemoryTaskManager.removeAllEpics();
                        fm.removeAllEpics();
                        System.out.println("Список эпиков очищен.\n");
                    }
                    break;
                case "3":
                    System.out.println("Введите id задачи, которую хотите найти.");
                    int idForSearch = checkNextInt();

                    if (inMemoryTaskManager.isTaskAddedByID(idForSearch)
                            || inMemoryTaskManager.isEpicAddedByID(idForSearch)
                            || inMemoryTaskManager.isSubtaskAddedByID(idForSearch)) {
                        if (isTask && inMemoryTaskManager.isTaskAddedByID(idForSearch)) {
                            System.out.println(inMemoryTaskManager.findTaskByID(idForSearch));
                        } else if (inMemoryTaskManager.isEpicAddedByID(idForSearch) && typeOfTask.equals("2")) {
                            System.out.println(inMemoryTaskManager.findEpicByID(idForSearch));
                            System.out.println(inMemoryTaskManager.getFullDescOfAllSubtasksOfEpicById(idForSearch));
                        } else if (inMemoryTaskManager.isSubtaskAddedByID(idForSearch) && typeOfTask.equals("2")) {
                            System.out.println(inMemoryTaskManager.findSubtaskByID(idForSearch));
                        } else {
                            System.out.println("Не найдено, введён неверный формат.\n");
                        }
                    } else {
                        System.out.println("Задачи с таким id нет в вашем списке.\n");
                    }
                    break;
                case "4":
                    name = inputNameOfTask();
                    description = inputDescriptionOfTask();

                    if (isTask) {
                        fm.saveNewTask(inMemoryTaskManager.saveNewTask(new Task(name, description, temporaryID)));
                    } else {
                        System.out.println("Сколько шагов до вашей цели?");
                        int amountSteps = checkNextInt();

                        Epic newEpic = new Epic(name, description, temporaryID);
                        fm.saveNewEpic(inMemoryTaskManager.saveNewEpic(newEpic));
                        for (int i = 1; i <= amountSteps; i++) {
                            Subtask newSubtask = new Subtask(
                                    inputNameSubtaskOfEpic(), inputDescriptionSubtaskOfEpic(),
                                    temporaryID, newEpic.getId()
                            );
                            fm.saveNewSubtask(inMemoryTaskManager.saveNewSubtask(newSubtask));
                        }
                    }
                    System.out.println("Успешно сохранено!\n");
                    break;
                case "5":
                    System.out.println("Введите id элемента, который хотите изменить");
                    int idForUpdate = checkNextInt();

                    isElementByIdSaved = inMemoryTaskManager.isTaskAddedByID(idForUpdate)
                            || inMemoryTaskManager.isEpicAddedByID(idForUpdate);

                    if (!isElementByIdSaved) {
                        System.out.println("Не найдено задачи с таким id в вашем списке.\n");
                        continue;
                    }

                    printMenuForUpdate();
                    int choosingWhatToUpdate = checkNextInt();

                    switch (choosingWhatToUpdate) {
                        case 1:
                            if (isTask && inMemoryTaskManager.isTaskAddedByID(idForUpdate)) {
                                fm.updateTask(
                                        inMemoryTaskManager.updateTask(
                                                new Task(getNewValueForUpdate(),
                                                        inMemoryTaskManager.findTaskByID(idForUpdate).getDescription(),
                                                        idForUpdate
                                                )
                                        )
                                );
                                System.out.println(printMessageAboutSuccessfulFinishingOperation());
                            } else if (inMemoryTaskManager.isEpicAddedByID(idForUpdate)) {
                                fm.updateEpic(
                                        inMemoryTaskManager.updateEpic(new Epic(
                                                        getNewValueForUpdate(),
                                                        inMemoryTaskManager.findEpicByID(idForUpdate).getDescription(),
                                                        idForUpdate
                                                )
                                        )
                                );
                                System.out.println(printMessageAboutSuccessfulFinishingOperation());
                            } else {
                                System.out.println("Не найдено, введён неверный формат.\n");
                            }
                            break;
                        case 2:
                            if (isTask && inMemoryTaskManager.isTaskAddedByID(idForUpdate)) {
                                fm.updateTask(
                                        inMemoryTaskManager.updateTask(new Task(
                                                        inMemoryTaskManager.findTaskByID(idForUpdate).getName(),
                                                        getNewValueForUpdate(), idForUpdate
                                                )
                                        )
                                );
                                System.out.println(printMessageAboutSuccessfulFinishingOperation());
                            } else if (inMemoryTaskManager.isEpicAddedByID(idForUpdate)) {
                                fm.updateEpic(
                                        inMemoryTaskManager.updateEpic(new Epic(
                                                        inMemoryTaskManager.findEpicByID(idForUpdate).getName(),
                                                        getNewValueForUpdate(), idForUpdate
                                                )
                                        )
                                );
                                System.out.println(printMessageAboutSuccessfulFinishingOperation());
                            } else {
                                System.out.println("Не найдено, введён неверный формат.\n");
                            }
                            break;
                        case 3:
                            printMenuForProgresses();
                            System.out.println("Введите новое значение.");
                            int indexOfNewProgStatus = checkNextInt();

                            if (indexOfNewProgStatus >= 1 && indexOfNewProgStatus <= 3) {
                                Progress newStatus = Progress.valueOf(enumsProgress[indexOfNewProgStatus - 1]);

                                if (isTask && inMemoryTaskManager.isTaskAddedByID(idForUpdate)) {
                                    Task newTask = new Task(
                                            inMemoryTaskManager.findTaskByID(idForUpdate).getName(),
                                            inMemoryTaskManager.findTaskByID(idForUpdate).getDescription(),
                                            idForUpdate
                                    );
                                    newTask.setStatus(newStatus);
                                    fm.updateTask(inMemoryTaskManager.updateTask(newTask));

                                    System.out.println(printMessageAboutSuccessfulFinishingOperation());
                                } else if (inMemoryTaskManager.isEpicAddedByID(idForUpdate)) {
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
                                        fm.updateSubtask(inMemoryTaskManager.updateSubtask(newSubtask));
                                        System.out.println(printMessageAboutSuccessfulFinishingOperation());
                                    } else {
                                        System.out.println("Ошибка. Подзадачи с этим номером нет.\n");
                                    }
                                    System.out.println("Успешно сохранено!\n");
                                } else {
                                    System.out.println("Не найдено, введён неверный формат.\n");
                                }
                            } else {
                                System.out.println("Введите число от 1 до 3.\n");
                            }
                            break;
                        case 4:
                            if (isTask) {
                                System.out.println("Данная команда предназначена только для эпиков.\n");
                            } else if (inMemoryTaskManager.isEpicAddedByID(idForUpdate)) {
                                System.out.println("Введите id подзадачи, которую нужно сменить.");
                                int index = checkNextInt();
                                System.out.println("Введите новые значения.");

                                Epic epic = inMemoryTaskManager.findEpicByID(idForUpdate);
                                ArrayList<Integer> indexes = epic.getSubtasksIDs();

                                if (indexes.contains(index)) {
                                    Subtask newSubtask = new Subtask(inputNameSubtaskOfEpic(),
                                            inputDescriptionSubtaskOfEpic(), index, idForUpdate
                                    );
                                    fm.updateSubtask(inMemoryTaskManager.updateSubtask(newSubtask));
                                    System.out.println(printMessageAboutSuccessfulFinishingOperation());
                                } else {
                                    System.out.println("Ошибка. Подзадачи с этим id в этом эпике нет.\n");
                                }
                            } else {
                                System.out.println("Не найдено, введён неверный формат.\n");
                            }
                            break;
                        default:
                            System.out.println("Такой команды нет.\n");
                            break;
                    }
                    break;
                case "6":
                    System.out.println("Введите id элемента, который хотите удалить.");
                    int idForDelete = checkNextInt();
                    if (inMemoryTaskManager.isTaskAddedByID(idForDelete)) {
                        inMemoryTaskManager.deleteOneTaskByID(idForDelete);
                        fm.deleteOneTaskByID(idForDelete);
                        System.out.println(printMessageAboutSuccessfulFinishingOperation());
                    } else if (inMemoryTaskManager.isEpicAddedByID(idForDelete)) {
                        fm.deleteOneEpicByID(idForDelete);
                        System.out.println(printMessageAboutSuccessfulFinishingOperation());
                    } else if (inMemoryTaskManager.isSubtaskAddedByID(idForDelete)) {
                        fm.deleteOneSubtaskskByID(idForDelete);
                        System.out.println(printMessageAboutSuccessfulFinishingOperation());
                    } else {
                        System.out.println("Задачи с таким id нет в вашем списке.\n");
                    }
                    break;
                case "7":
                    System.out.println(inMemoryTaskManager.showListViewedTasks());
                    break;
                case "8":
                    System.out.println("Введите id эпика, который хотите посмотреть.");
                    int idForViewEpic = checkNextInt();
                    if (inMemoryTaskManager.isEpicAddedByID(idForViewEpic)) {
                        System.out.println(inMemoryTaskManager.findEpicByID(idForViewEpic));
                        System.out.println(inMemoryTaskManager.getFullDescOfAllSubtasksOfEpicById(idForViewEpic));
                        System.out.println();
                    } else {
                        System.out.println("Эпика с таким id не найдено.\n");
                    }
                    break;
                default:
                    System.out.println("А такой команды у нас ещё нет.\n");
                    break;
            }
        }
    }

    public static void printMenuOfTypes() {
        System.out.println("С каким видом задачи хотите работать?");
        System.out.println("1 - Задача (одно действие для достижения результата),");
        System.out.println("2 - Эпик (несколько действий для достижения результата).");
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
        System.out.println("7 - посмотреть список просмотренных задач");
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
                messageWithAllEpics = messageWithAllEpics
                        + inMemoryTaskManager.getFullDescOfAllSubtasksOfEpicById(epic.getId());
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
            System.out.println("Введено не число. Повторите ввод.\n");
            scanner.nextLine();
        }
    }

    public static String getNewValueForUpdate() {
        System.out.println("Введите новое значение.");
        return scanner.nextLine();
    }

    public static String printMessageAboutSuccessfulFinishingOperation() {
        return "Выполнено успешно" + "\n";
    }
}