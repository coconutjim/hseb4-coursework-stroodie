package model;

import model.user_util.UserRole;
import org.joda.time.DateTime;

import javax.persistence.*;

/**
 * User
 */
@Entity
@Table(name = "users")
public class User {

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Id
    @GeneratedValue
    private int id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private UserRole role;

    @Column(nullable = false)
    private boolean activated;

    @Column(nullable = false)
    private String activationToken;

    @Column(nullable = false)
    private DateTime activationTokenExpiresAt;

    private String passwordRecoveryToken;

    private DateTime passwordRecoveryTokenExpiresAt;

    public User() {
    }

    public User(String email, String passwordHash, String firstName, String lastName,
                UserRole role, String activationToken, DateTime activationTokenExpiresAt) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.activated = false;
        this.activationToken = activationToken;
        this.activationTokenExpiresAt = activationTokenExpiresAt;
    }

    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public int getId() {
        return id;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getLastName() {
        return lastName;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public String getActivationToken() {
        return activationToken;
    }

    public void setActivationToken(String activationToken) {
        this.activationToken = activationToken;
    }

    public DateTime getActivationTokenExpiresAt() {
        return activationTokenExpiresAt;
    }

    public void setActivationTokenExpiresAt(DateTime activationTokenExpiresAt) {
        this.activationTokenExpiresAt = activationTokenExpiresAt;
    }

    public String getPasswordRecoveryToken() {
        return passwordRecoveryToken;
    }

    public void setPasswordRecoveryToken(String passwordRecoveryToken) {
        this.passwordRecoveryToken = passwordRecoveryToken;
    }

    public DateTime getPasswordRecoveryTokenExpiresAt() {
        return passwordRecoveryTokenExpiresAt;
    }

    public void setPasswordRecoveryTokenExpiresAt(DateTime passwordRecoveryTokenExpiresAt) {
        this.passwordRecoveryTokenExpiresAt = passwordRecoveryTokenExpiresAt;
    }
}
