import java.time.LocalDate;

public class Task {
    private int id;
    String text;
    boolean done;
    String priority;
    LocalDate date;

    // For loading from DB
 public Task(int id, String text, String priority, boolean done, LocalDate date) {
    this.id = id;
    this.text = text;
    this.priority = priority;
    this.done = done;
    this.date = date;
}

    // For new task (before DB gives id)
    public Task(String text, String priority, LocalDate date) {
        this.text = text;
        this.priority = priority;
        this.date = date;
    }

    public int getId() {
        return id;
    }
}