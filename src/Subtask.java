public class Subtask extends Task {
    public Subtask(String name, Progress status) {
        super(name, status);
    }

    @Override
    public String toString() {
        return this.getName() + "'" + ", статус прогресса этой подзадачи: " + this.getStatus() + "\n";
    }
}
