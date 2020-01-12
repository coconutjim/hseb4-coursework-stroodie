package model.service;

import model.User;

import java.util.List;

/**
 * User Service
 */
public interface UserService {
    void saveUser(User user);
    List<User> getAllUsers();
    User getUserById(int id);
    User getUserByEmail(String email);
    void updateUser(User user);
    void deleteUser(User user);
}
