package model.dao;

import model.*;

import java.util.List;
import java.util.Map;

/**
 * Message DAO
 */
public interface MessageDAO {

    void save(Message message);
    List<Message> getAll();
    Message getById(int id);
    void update(Message message);
    void delete(Message message);
    List<Message> getByDiscussion(Discussion discussion);
    Map<MessageUnion, List<Message>> getUnionsByDiscussion(Discussion discussion);

    int getUid();
}
