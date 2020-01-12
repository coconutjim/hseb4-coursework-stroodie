package model.service;

import model.*;

import java.util.List;

/**
 * Notification service
 */
public interface NotificationService {

    List<Notification> getAllNotifications(User user);
    void setNotificationsForSeminarRead(Seminar seminar, User user);
    void setNotificationsForDiscussionRead(Discussion discussion, User user);
}
