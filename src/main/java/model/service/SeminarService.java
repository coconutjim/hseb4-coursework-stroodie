package model.service;

import model.Seminar;
import model.SeminarParticipation;
import model.User;

import java.util.List;

/**
 * Seminar service
 */
public interface SeminarService {
    void saveSeminar(Seminar seminar);
    List<Seminar> getAllSeminars(boolean withUsers, boolean withOntology, boolean withDiscussions);
    List<Seminar> getSeminarsByUser(User user);
    Seminar getSeminarById(int id, boolean withUsers, boolean withOntology, boolean withDiscussions);
    void updateSeminar(Seminar seminar, List<SeminarParticipation> participations);
    void deleteSeminar(Seminar seminar);
}
