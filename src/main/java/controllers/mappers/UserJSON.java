package controllers.mappers;

/**
 * Mapper for User JSON
 */
public class UserJSON {

    private String email;

    private String password;

    private String passwordConfirm;

    private String firstName;

    private String lastName;

    private String oldPassword;

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getPasswordConfirm() {
        return passwordConfirm;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getOldPassword() {
        return oldPassword;
    }
}
