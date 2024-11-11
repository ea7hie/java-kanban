import java.util.HashMap;

public class TaskManager {
    HashMap<Integer, Task> allTasks;
    HashMap<Integer, Epic> allEpics;
    String[] enumsProgress = {"NEW", "IN_PROGRESS", "DONE"};

    public TaskManager() {
        this.allTasks = new HashMap<>();
        this.allEpics = new HashMap<>();
    }

    public void saveNewTask(Task task) {
        allTasks.put(task.getId(), task);
    }

    public void saveNewEpic(Epic epic) {
        allEpics.put(epic.getId(), epic);
    }

    public void printAllTasks() {
        if (allTasks.isEmpty()) {
            System.out.println("Список задач пуст.");
        } else {
            for (Task task : allTasks.values()) {
                System.out.println(task);
            }
        }
    }

    public void printAllEpics() {
        if (allEpics.isEmpty()) {
            System.out.println("Список эпиков пуст.");
        } else {
            for (Epic epic : allEpics.values()) {
                System.out.println(epic);
            }
        }
    }

    public void removeAllTasks() {
        allTasks.clear();
        System.out.println("Список задач очищен.");
    }

    public void removeAllEpics() {
        allEpics.clear();
        System.out.println("Список эпиков очищен.");
    }

    //отличие от isTaskAddedByID: возращает более точную инфу, почему элемент не найден
    public String findTaskByID(int id) {
        if (!allTasks.isEmpty()) {
            for (Task task : allTasks.values()) {
                if (task.getId() == id) {
                    return task.toString();
                }
            }
            return "Задачи с таким id нет. Проверьте ввод или попробуйте запустить поиск по этому id среди эпиков.";
        }
        return "Список задач пуст.";
    }

    public String findEpicByID(int id) {
        if (!allEpics.isEmpty()) {
            for (Epic epic : allEpics.values()) {
                if (epic.getId() == id) {
                    return epic.toString();
                }
            }
            return "Эпика с таким id нет. Проверьте ввод или попробуйте запустить поиск по этому id среди задач.";
        }
        return "Список эпиков пуст.";
    }

    public boolean isTaskAddedByID(int id) {
        if (!allTasks.isEmpty()) {
            for (Task task : allTasks.values()) {
                if (task.getId() == id) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isEpicAddedByID(int id) {
        if (!allEpics.isEmpty()) {
            for (Epic epic : allEpics.values()) {
                if (epic.getId() == id) {
                    return true;
                }
            }
        }
        return false;
    }

    public String updateName(int id, String newValue, boolean isTask) {
        if (isTask) {
            allTasks.get(id).setName(newValue);
        } else {
            allEpics.get(id).setName(newValue);
        }
        return "Выполнено успешно";
    }

    public String updateDescription(int id, String description, boolean isTask) {
        if (isTask) {
            allTasks.get(id).setDescription(description);
        } else {
            allEpics.get(id).setDescription(description);
        }
        return "Выполнено успешно";
    }

    public String updateProgressTask(int id, int indexOfEnum) {
        allTasks.get(id).setStatus(Progress.valueOf(enumsProgress[indexOfEnum - 1]));
        return "Выполнено успешно";
    }

    public String updateProgressSubtaskOfEpic(int id, int index, int indexOfEnum) {
        return allEpics.get(id).setStatus((index - 1), Progress.valueOf(enumsProgress[indexOfEnum - 1]));
        //find the epic in HashMap by ID; call its setStatus of subtask(find by index) to an array enumsProgress[] elem
    }

    public String updateNameOfSubtaskOfEpic(int id, int index, String newValue, Progress status) {
        return allEpics.get(id).setSubtasks((index - 1), newValue, status);
    }

    public String deleteOneElementByID(int idForDelete, boolean isTask) {
        if (isTask) {
            allTasks.remove(idForDelete);
        } else {
            allEpics.remove(idForDelete);
        }
        return "Выполнено успешно";
    }

    public String showOneEpicByID(int idFotViewEpic) {
        return allEpics.get(idFotViewEpic).showAllSubtasks();
    }
}