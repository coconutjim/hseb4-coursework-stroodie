package model.dao;

import model.*;

import java.util.List;

/**
 * Notification DAO
 */
public interface NotificationDAO {

    List<Notification> getAllNotifications(User user);

    void createNotificationsForSeminar(Seminar seminar);
    void createNotificationsForDiscussion(Seminar seminar, Discussion discussion);
    void createNotificationsForMessage(Seminar seminar, Message message);

    void createNotificationsForSeminar(Seminar seminar, List<SeminarParticipation> participations);
    void createNotificationsForDiscussion(Discussion discussion, List<SeminarParticipation> participations);

    void setNotificationsForSeminarRead(Seminar seminar, User user);
    void setNotificationsForDiscussionRead(Discussion discussion, User user);

    void deleteNotificationsForSeminar(Seminar seminar);
    void deleteNotificationsForDiscussion(Discussion discussion);
    void deleteNotificationsForMessage(Message message);
}
