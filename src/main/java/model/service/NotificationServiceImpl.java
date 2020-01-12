package model.service;

import model.*;
import model.dao.NotificationDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Notification service implementation
 */
@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationDAO notificationDAO;

    @Override
    public List<Notification> getAllNotifications(User user) {
        return notificationDAO.getAllNotifications(user);
    }

    @Override
    @Transactional
    public void setNotificationsForSeminarRead(Seminar seminar, User user) {
       notificationDAO.setNotificationsForSeminarRead(seminar, user);
    }

    @Override
    @Transactional
    public void setNotificationsForDiscussionRead(Discussion discussion, User user) {
        notificationDAO.setNotificationsForDiscussionRead(discussion, user);
    }
}
