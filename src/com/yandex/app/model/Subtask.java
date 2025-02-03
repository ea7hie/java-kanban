package com.yandex.app.model;

public class Subtask extends Task {
    private int idOfSubtaskEpic;
    static final String FOOTER_OF_SUBTASK = "- - - - - - - - - - - - - - - - - - - - - - - - - - - ";

    public Subtask(String name, String description, int id, int idOfSubtaskEpic, int duration, String startTime) {
        super(name, description, id, duration, startTime);
        this.idOfSubtaskEpic = idOfSubtaskEpic;
    }

    public int getIdOfSubtaskEpic() {
        return idOfSubtaskEpic;
    }

    public void setIdOfSubtaskEpic(int idOfSubtaskEpic) {
        this.idOfSubtaskEpic = idOfSubtaskEpic;
    }

    @Override
    public String toString() {
        return String.format(" '%s', %sописание: '%s', %sid=%d, %sстатус прогресса всего эпика: %s, %s" +
                        "начало выполнения этой подзадачи: %s, %sпродолжительность: %s мин, " +
                        "%sконец выполнения этой подзадачи: %s%s%s%s",
                this.getName(), NEW_LINE, this.getDescription(), NEW_LINE, this.getId(), NEW_LINE, this.getStatus(),
                NEW_LINE, this.getStartTime(), NEW_LINE, this.getDuration(), NEW_LINE, this.getEndTime(), NEW_LINE,
                FOOTER_OF_SUBTASK, NEW_LINE);
    }
}