package Models;

public class Author {
    public String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "Author{" +
                "email='" + email + '\'' +
                '}';
    }
}
