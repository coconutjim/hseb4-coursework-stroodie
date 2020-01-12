package model.service;

import model.*;

import java.util.List;
import java.util.Map;

/**
 * Discussion service
 */
public interface DiscussionService {

    void saveDiscussion(Seminar seminar, Discussion discussion);
    List<Discussion> getAllDiscussions(boolean withMessages);
    Discussion getDiscussionById(int id, boolean withMessages);
    void updateDiscussion(Discussion discussion, List<SeminarParticipation> participations);
    void deleteDiscussion(Discussion discussion);

    SavedGraph getSavedGraph(Discussion discussion, User user);
    void updateSavedGraph(SavedGraph savedGraph);

    void saveUnion(MessageUnion union, List<Message> freeMessages, Map<MessageUnion, List<Message>> unionMessages);
    void updateUnion(MessageUnion union);
    void deleteUnion(List<Message> messages);

    void saveThesaurusUnit(ThesaurusUnit unit);
    void updateThesaurusUnit(ThesaurusUnit unit);
    void deleteThesaurusUnit(ThesaurusUnit unit);
}
