package model.dao;


import model.Seminar;
import model.User;

import java.util.List;

/**
 * Seminar DAO
 */
public interface SeminarDAO {
    void save(Seminar seminar);
    List<Seminar> getAll(boolean withUsers, boolean withOntology, boolean withDiscussions);
    List<Seminar> getByUser(User user);
    Seminar getById(int id, boolean withUsers, boolean withOntology, boolean withDiscussions);
    void update(Seminar seminar);
    void delete(Seminar seminar);
}
