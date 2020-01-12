package model.dao;

import model.User;

import java.util.List;

/**
 * User DAO
 */
public interface UserDAO {
    void save(User user);
    List<User> getAll();
    User getById(int id);
    User getByEmail(String email);
    void update(User user);
    void delete(User user);
}
