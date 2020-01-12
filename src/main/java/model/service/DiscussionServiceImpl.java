package model.service;

import model.*;
import model.dao.DiscussionDAO;
import model.dao.NotificationDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Discussion service implementation
 */
@Service
public class DiscussionServiceImpl implements DiscussionService {

    @Autowired
    private DiscussionDAO discussionDAO;

    @Autowired
    private NotificationDAO notificationDAO;

    @Override
    @Transactional
    public void saveDiscussion(Seminar seminar, Discussion discussion) {
        discussionDAO.save(discussion);
        notificationDAO.createNotificationsForDiscussion(seminar, discussion);
    }

    @Override
    public List<Discussion> getAllDiscussions(boolean withMessages) {
        return discussionDAO.getAll(withMessages);
    }

    @Override
    public Discussion getDiscussionById(int id, boolean withMessages) {
        return discussionDAO.getById(id, withMessages);
    }

    @Override
    @Transactional
    public void updateDiscussion(Discussion discussion, List<SeminarParticipation> participations) {
        discussionDAO.update(discussion);
        notificationDAO.createNotificationsForDiscussion(discussion, participations);
    }

    @Override
    @Transactional
    public void deleteDiscussion(Discussion discussion) {
        discussionDAO.delete(discussion);
    }

    @Override
    public SavedGraph getSavedGraph(Discussion discussion, User user) {
        return discussionDAO.getSavedGraph(discussion, user);
    }

    @Override
    @Transactional
    public void updateSavedGraph(SavedGraph savedGraph) {
        discussionDAO.updateSavedGraph(savedGraph);
    }

    @Override
    @Transactional
    public void saveUnion(MessageUnion union, List<Message> freeMessages, Map<MessageUnion, List<Message>> unionMessages) {
        discussionDAO.saveUnion(union, freeMessages, unionMessages);
    }

    @Override
    @Transactional
    public void updateUnion(MessageUnion union) {
        discussionDAO.updateUnion(union);
    }

    @Override
    @Transactional
    public void deleteUnion(List<Message> messages) {
        discussionDAO.deleteUnion(messages);
    }

    @Override
    @Transactional
    public void saveThesaurusUnit(ThesaurusUnit unit) {
        discussionDAO.saveThesaurusUnit(unit);
    }

    @Override
    @Transactional
    public void updateThesaurusUnit(ThesaurusUnit unit) {
        discussionDAO.updateThesaurusUnit(unit);
    }

    @Override
    @Transactional
    public void deleteThesaurusUnit(ThesaurusUnit unit) {
        discussionDAO.deleteThesaurusUnit(unit);
    }
}
