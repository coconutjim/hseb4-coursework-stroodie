package model.service;

import model.Seminar;
import model.SeminarParticipation;
import model.User;
import model.dao.NotificationDAO;
import model.dao.SeminarDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Seminar service implementation
 */
@Service
public class SeminarServiceImpl implements SeminarService {

    @Autowired
    private SeminarDAO seminarDAO;

    @Autowired
    private NotificationDAO notificationDAO;

    @Override
    @Transactional
    public void saveSeminar(Seminar seminar) {
        seminarDAO.save(seminar);
        notificationDAO.createNotificationsForSeminar(seminar);
    }

    @Override
    public List<Seminar> getAllSeminars(boolean withUsers, boolean withOntology, boolean withDiscussions) {
        return seminarDAO.getAll(withUsers, withOntology, withDiscussions);
    }

    @Override
    public List<Seminar> getSeminarsByUser(User user) {
        return seminarDAO.getByUser(user);
    }

    @Override
    public Seminar getSeminarById(int id, boolean withUsers, boolean withOntology, boolean withDiscussions) {
        return seminarDAO.getById(id, withUsers, withOntology, withDiscussions);
    }

    @Override
    @Transactional
    public void updateSeminar(Seminar seminar, List<SeminarParticipation> participations) {
        seminarDAO.update(seminar);
        notificationDAO.createNotificationsForSeminar(seminar, participations);
    }

    @Override
    @Transactional
    public void deleteSeminar(Seminar seminar) {
        seminarDAO.delete(seminar);
    }
}
