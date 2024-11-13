import com.yandex.taskTrackerApp.model.Epic;
import com.yandex.taskTrackerApp.model.Progress;
import com.yandex.taskTrackerApp.model.Subtask;
import com.yandex.taskTrackerApp.model.Task;
import com.yandex.taskTrackerApp.service.TaskManager;

import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    public static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        TaskManager taskManager = new TaskManager();
        String[] enumsProgress = {"NEW", "IN_PROGRESS", "DONE"};
        int idOfNewTask = 1;

        System.out.println("Добро пожаловать в Трекер Задач, в ваш персональный помощник!");
        String typeOfTask;
        while (true) {
            printMenuOfTypes();

            do {
                System.out.println("Введите нужную команду соответствующей цифрой: 0(выход), 1(задача) или 2(эпик).");
                typeOfTask = scanner.next();
            } while (!typeOfTask.equals("1") && !typeOfTask.equals("2") && !typeOfTask.equals("0"));

            boolean isTask = typeOfTask.equals("1");
            //isTack = false if typeOfTask.equals("2") -> type is epic!

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
            Progress status = Progress.NEW;

            switch (cmd) {
                case "1":
                    if (isTask) {
                        System.out.println(taskManager.printAllTasks());
                    } else {
                        System.out.println(taskManager.printAllEpics());
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
                    int id = checkNextInt();

                    if (isTask) {
                        System.out.println(taskManager.findTaskByID(id));
                    } else {
                        System.out.println(taskManager.findEpicByID(id));
                    }
                    break;
                case "4":
                    name = inputNameOfTask();
                    description = inputDescriptionOfTask();
                    int idNewEpic =  generateNewID(idOfNewTask);

                    if (isTask) {
                        taskManager.saveNewTask(new Task(name, description,
                                generateNewID(idOfNewTask), status));
                    } else {
                        System.out.println("Сколько шагов до вашей цели?");
                        int amountSteps = checkNextInt();
                        for (int i = 1; i <= amountSteps; i++) {
                            Subtask newSubtask = new Subtask(inputSubtaskOfEpic(), status, idNewEpic);
                            subtasks.add(newSubtask);
                        }

                        taskManager.saveNewEpic(new Epic(name, description,
                               idNewEpic, status, subtasks));
                    }
                    System.out.println("Успешно сохранено!");
                    idOfNewTask++;
                    break;
                case "5":
                    System.out.println("Введите id элемента, который хотите изменить");
                    int idForUpdate = checkNextInt();

                    boolean isElementDyIdSaved = taskManager.isTaskAddedByID(idForUpdate)
                            || taskManager.isEpicAddedByID(idForUpdate);

                    if (isElementDyIdSaved) {
                        printMenuForUpdate();
                        int choosingWhatToUpdate = checkNextInt();

                        switch (choosingWhatToUpdate) {
                            case 1:
                                if (isTask) {
                                    taskManager.updateTask(
                                            new Task(
                                                    getNewValueForUpdate(),
                                                    taskManager.getAllTasks().get(idForUpdate).getDescription(),
                                                    idForUpdate, status
                                            )
                                    );
                                } else {
                                    taskManager.updateEpic(
                                            new Epic(
                                                    getNewValueForUpdate(),
                                                    taskManager.getAllEpics().get(idForUpdate).getDescription(),
                                                    idForUpdate, status,
                                                    taskManager.getAllEpics().get(idForUpdate).getSubtasks()
                                            )
                                    );
                                }
                                break;
                            case 2:
                                if (isTask) {
                                    taskManager.updateTask(
                                            new Task(
                                                    taskManager.getAllTasks().get(idForUpdate).getName(),
                                                    getNewValueForUpdate(), idForUpdate, status
                                            )
                                    );
                                } else {
                                    taskManager.updateEpic(
                                            new Epic(
                                                    taskManager.getAllEpics().get(idForUpdate).getName(),
                                                    getNewValueForUpdate(), idForUpdate, status,
                                                    taskManager.getAllEpics().get(idForUpdate).getSubtasks()
                                            )
                                    );
                                }
                                break;
                            case 3:
                                printMenuForProgresses();
                                System.out.println("Введите новое значение.");
                                int indexOfNewProgSt = checkNextInt();

                                if (indexOfNewProgSt >= 1 && indexOfNewProgSt <= 3) {
                                    Progress newStatus = Progress.valueOf(enumsProgress[indexOfNewProgSt - 1]);
                                    if (isTask) {
                                        taskManager.updateTask(
                                                new Task(
                                                        taskManager.getAllTasks().get(idForUpdate).getName(),
                                                        taskManager.getAllTasks().get(idForUpdate).getDescription(),
                                                        idForUpdate, newStatus
                                                )
                                        );
                                    } else {
                                        System.out.println("Введите номер подзадачи, которую нужно сменить.");
                                        int index = checkNextInt();
                                        subtasks =  taskManager.getAllEpics().get(idForUpdate).getSubtasks();
                                        if (subtasks.size() >= index) {
                                            subtasks.get(index - 1).setStatus(newStatus);
                                            Epic newEpic =  new Epic(
                                                    taskManager.getAllEpics().get(idForUpdate).getName(),
                                                    taskManager.getAllEpics().get(idForUpdate).getDescription(),
                                                    idForUpdate, status, subtasks
                                            );
                                            newEpic.setStatus(taskManager.checkProgressStatusOfEpic(newEpic));
                                            taskManager.updateEpic(newEpic);
                                        } else {
                                            System.out.println("Ошибка. Подзадачи с этим номером нет.");
                                        }
                                    }
                                } else {
                                    System.out.println("Введите число от 1 до 3.");
                                }
                                break;
                            case 4:
                                if (isTask) {
                                    System.out.println("Данная команда предназначена только для эпиков.");
                                } else {
                                    System.out.println("Введите номер подзадачи, которую нужно сменить.");
                                    int index = checkNextInt();
                                    System.out.println("Введите новое значение.");
                                    String newValue = scanner.nextLine();
                                    subtasks =  taskManager.getAllEpics().get(idForUpdate).getSubtasks();
                                    if (subtasks.size() >= index) {
                                        subtasks =  taskManager.getAllEpics().get(idForUpdate).getSubtasks();
                                        subtasks.get(index - 1).setName(newValue);
                                        taskManager.updateEpic(
                                                new Epic(
                                                        taskManager.getAllEpics().get(idForUpdate).getName(),
                                                        taskManager.getAllEpics().get(idForUpdate).getDescription(),
                                                        idForUpdate, status, subtasks
                                                )
                                        );
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
                    System.out.println(taskManager.deleteOneElementByID(checkNextInt(), isTask));
                    break;
                case "7":
                    System.out.println("Введите id эпика, который хотите посмотреть.");
                    int idFotViewEpic = checkNextInt();
                    if (taskManager.isEpicAddedByID(idFotViewEpic)) {
                        System.out.println(taskManager.showOneEpicByID(idFotViewEpic));
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

    public static String inputNameOfTask() {
        scanner.nextLine();
        System.out.println("Введите название вашей задачи (конечный результат, цель)");
        return scanner.nextLine();
    }

    public static int generateNewID(int idOfNewTask) {
        return (idOfNewTask * 11 - 7);
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
