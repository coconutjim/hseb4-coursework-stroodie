package model.service;

import model.*;

import java.util.List;

/**
 * Message service
 */
public interface MessageService {

    void saveMessage(Seminar seminar, Message message);
    List<Message> getAllMessages();
    Message getMessageById(int id);
    void updateMessage(Message message);
    void deleteMessage(Message message);
}
