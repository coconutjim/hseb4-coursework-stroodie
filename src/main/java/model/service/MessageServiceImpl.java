package model.service;

import model.*;
import model.dao.MessageDAO;
import model.dao.NotificationDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Message service implementation
 */
@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageDAO messageDAO;

    @Autowired
    private NotificationDAO notificationDAO;

    @Override
    @Transactional
    public void saveMessage(Seminar seminar, Message message) {
        messageDAO.save(message);
        notificationDAO.createNotificationsForMessage(seminar, message);
    }

    @Override
    public List<Message> getAllMessages() {
        return messageDAO.getAll();
    }

    @Override
    public Message getMessageById(int id) {
        return messageDAO.getById(id);
    }

    @Override
    @Transactional
    public void updateMessage(Message message) {
        messageDAO.update(message);
    }

    @Override
    @Transactional
    public void deleteMessage(Message message) {
        messageDAO.delete(message);
    }
}
