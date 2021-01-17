package lk.ijse.dep.web.dto;

public class UserDTO {
    private String username;
    private String password;

    public UserDTO(String username, String password) {
        this.setUsername(username);
        this.setPassword(password);
    }

    @Override
    public String toString() {
        return "UserDTO{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
    }

    public UserDTO() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
