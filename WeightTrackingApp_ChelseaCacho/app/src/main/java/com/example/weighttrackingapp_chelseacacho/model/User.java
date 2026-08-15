// Represents one user account in the application.
public class User {

    // Unique ID assigned to the user.
    private int userId;

    // User's full name.
    private String fullName;

    // User's email address used for login.
    private String email;

    // User's stored password value.
    private String password;

    // Creates a User object with all required account information.
    public User(int userId, String fullName, String email, String password) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.password = password;
    }

    // Returns the user's unique ID.
    public int getUserId() {
        return userId;
    }

    // Returns the user's full name.
    public String getFullName() {
        return fullName;
    }

    // Returns the user's email address.
    public String getEmail() {
        return email;
    }

    // Returns the user's stored password value.
    public String getPassword() {
        return password;
    }
}