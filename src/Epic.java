import java.util.ArrayList;

public class Epic extends Task{
    ArrayList<Subtask> subtasks;
    public Epic(String name, String description, int id, Progress status, ArrayList<Subtask> subtasks) {
        super(name, description, id, status);
        this.subtasks = subtasks;
    }

    public String setSubtasks(int index, String newValue, Progress status) {
        if (index < subtasks.size()) {
            subtasks.remove(index);
            subtasks.add(index, new Subtask(newValue, status));
            return "Выполнено успешно";
        }
        return "Ошибка. Подзадачи с этим номером нет.";
    }

    @Override
    public String toString() {
        String fullDescriptionAboutEpic = "Эпик '" + this.getName() + "'" +
                ", \nописание: '" + this.getDescription() + "'" +
                ", \nid=" + this.getId() +
                ", \nстатус прогресса всего эпика: " + this.getStatus() + "\n";

        for (int i = 0; i < subtasks.size(); i++) {
            fullDescriptionAboutEpic = fullDescriptionAboutEpic + "Подзадача № " + (i + 1) + " '"
                    + subtasks.get(i).toString();
        }

        return fullDescriptionAboutEpic;
    }

    public String setStatus(int index, Progress status) {
        if (index < subtasks.size()) {
            subtasks.get(index).setStatus(status);
            this.setStatus(checkProgressStatusOfEpic(this));
            return "Выполнено успешно";
        }
        return "Ошибка. Подзадачи с этим номером нет.";
    }

    public Progress checkProgressStatusOfEpic(Epic epic) {
        int counter = 0;
        for (Subtask subtask : epic.subtasks) {
            if (subtask.getStatus().equals(Progress.NEW)) {
                counter++;
            }
        }
        if (counter == epic.subtasks.size()) {
            return Progress.NEW;
        }

        counter = 0;
        for (Subtask subtask : epic.subtasks) {
            if (subtask.getStatus().equals(Progress.DONE)) {
                counter++;
            }
        }
        if (counter == epic.subtasks.size()) {
            return Progress.DONE;
        }

        return Progress.IN_PROGRESS;
    }

    public String showAllSubtasks() {
        String listSubtasks = "";
        for (int i = 0; i < subtasks.size(); i++) {
            listSubtasks = listSubtasks + "Подзадача № " + (i + 1) + " '"
                    + subtasks.get(i).toString();
        }

        return listSubtasks;
    }
}

