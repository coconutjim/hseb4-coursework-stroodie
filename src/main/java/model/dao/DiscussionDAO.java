package model.dao;

import model.*;

import java.util.List;
import java.util.Map;

/**
 * Discussion DAO
 */
public interface DiscussionDAO {
    void save(Discussion discussion);
    List<Discussion> getAll(boolean withMessages);
    Discussion getById(int id, boolean withMessages);
    void update(Discussion discussion);
    void delete(Discussion discussion);

    SavedGraph getSavedGraph(Discussion discussion, User user);
    void updateSavedGraph(SavedGraph savedGraph);

    void saveUnion(MessageUnion union, List<Message> freeMessages, Map<MessageUnion, List<Message>> unionMessages);
    void updateUnion(MessageUnion union);
    void deleteUnion(List<Message> messages);

    void saveThesaurusUnit(ThesaurusUnit unit);
    void updateThesaurusUnit(ThesaurusUnit unit);
    void deleteThesaurusUnit(ThesaurusUnit unit);
}
