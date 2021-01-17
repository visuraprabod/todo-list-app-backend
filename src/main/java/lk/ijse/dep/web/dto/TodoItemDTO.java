package lk.ijse.dep.web.dto;

import lk.ijse.dep.web.util.Priority;
import lk.ijse.dep.web.util.Status;

public class TodoItemDTO {
    private Integer id;
    private String text;
    private Priority priority = Priority.PRIORITY1;
    private Status status = Status.NOT_COMPLETED;
    private String username;

    public TodoItemDTO(Integer id, String text, Priority priority, Status status, String username) {
        this.setId(id);
        this.setText(text);
        this.setPriority(priority);
        this.setStatus(status);
        this.setUsername(username);
    }

    public TodoItemDTO() {
    }


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "TodoItemDTO{" +
                "id=" + id +
                ", text='" + text + '\'' +
                ", priority=" + priority +
                ", status=" + status +
                ", username='" + username + '\'' +
                '}';
    }
}
