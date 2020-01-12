package model.dao;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import controllers.mappers.util.MessageFixedPoint;
import model.*;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Discussion DAO Implementation
 */
@Repository
public class DiscussionDAOImpl implements DiscussionDAO {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private MessageDAO messageDAO;

    @Autowired
    private NotificationDAO notificationDAO;

    @Override
    public void save(Discussion discussion) {
        em.persist(discussion);
    }

    @Override
    public List<Discussion> getAll(boolean withMessages) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Discussion> cq = builder.createQuery(Discussion.class);
        Root<Discussion> root = cq.from(Discussion.class);
        cq.select(root);
        cq.orderBy(builder.asc(root.get("id")));

        List<Discussion> discussions = em.createQuery(cq).getResultList();
        if (discussions != null) {
            for (Discussion discussion : discussions) {
                if (withMessages) {
                    discussion.setMessages(messageDAO.getByDiscussion(discussion));
                    discussion.setUnions(messageDAO.getUnionsByDiscussion(discussion));
                }
                discussion.setThesaurus(getThesaurusUnitsByDiscussion(discussion));
            }
        }
        return discussions;
    }

    @Override
    public Discussion getById(int id, boolean withMessages) {
        Discussion discussion = em.find(Discussion.class, id);
        if (withMessages) {
            discussion.setMessages(messageDAO.getByDiscussion(discussion));
            discussion.setUnions(messageDAO.getUnionsByDiscussion(discussion));
        }
        discussion.setThesaurus(getThesaurusUnitsByDiscussion(discussion));
        return discussion;
    }

    @Override
    public void update(Discussion discussion) {
        em.merge(discussion);
    }

    @Override
    public void delete(Discussion discussion) {
        notificationDAO.deleteNotificationsForDiscussion(discussion);
        List<Message> messages = messageDAO.getByDiscussion(discussion);
        if (messages != null) {
            for (Message message : messages) {
                message.setPrevious(null);
                messageDAO.update(message);
            }
            for (Message message : messages) {
                messageDAO.delete(message);
            }
        }
        List<SavedGraph> savedGraphs = getSaveGraphsByDiscussion(discussion);
        if (savedGraphs != null) {
            for (SavedGraph savedGraph : savedGraphs) {
                em.remove(savedGraph);
            }
        }
        List<ThesaurusUnit> units = getThesaurusUnitsByDiscussion(discussion);
        if (units != null) {
            for (ThesaurusUnit unit : units) {
                em.remove(unit);
            }
        }
        em.remove(discussion);
    }

    @Override
    public SavedGraph getSavedGraph(Discussion discussion, User user) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<SavedGraph> cq = builder.createQuery(SavedGraph.class);
        Root<SavedGraph> root = cq.from(SavedGraph.class);

        Predicate[] predicates = new Predicate[2];
        predicates[0] = builder.equal(root.get("discussion"), discussion.getId());
        predicates[1] = builder.equal(root.get("user"), user.getId());

        cq.where(predicates);
        List<SavedGraph> results = em.createQuery(cq).getResultList();
        return ( results == null || results.isEmpty() ) ? null : results.get(0);
    }

    @Override
    public void updateSavedGraph(SavedGraph savedGraph) {
        SavedGraph old = getSavedGraph(savedGraph.getDiscussion(), savedGraph.getUser());
        if (old == null) {
            em.persist(savedGraph);
        }
        else {
            old.setPositions(savedGraph.getPositions());
            em.merge(old);
        }
    }

    private List<SavedGraph> getSaveGraphsByDiscussion(Discussion discussion) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<SavedGraph> cq = builder.createQuery(SavedGraph.class);
        Root<SavedGraph> root = cq.from(SavedGraph.class);
        cq.where(builder.equal(root.get("discussion"), discussion.getId()));
        return em.createQuery(cq).getResultList();
    }

    private List<ThesaurusUnit> getThesaurusUnitsByDiscussion(Discussion discussion) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<ThesaurusUnit> cq = builder.createQuery(ThesaurusUnit.class);
        Root<ThesaurusUnit> root = cq.from(ThesaurusUnit.class);
        cq.where(builder.equal(root.get("discussion"), discussion.getId()));
        cq.orderBy(builder.asc(root.get("id")));
        return em.createQuery(cq).getResultList();
    }

    @Override
    public void saveUnion(MessageUnion union, List<Message> freeMessages, Map<MessageUnion, List<Message>> unionMessages) {
        union.setUid(messageDAO.getUid());
        em.persist(union);
        em.flush();
        for (Message message : freeMessages) {
            message.setUnion(union);
            messageDAO.update(message);
        }
        for (Map.Entry<MessageUnion, List<Message>> entry : unionMessages.entrySet()) {
            for (Message message : entry.getValue()) {
                message.setUnion(union);
                messageDAO.update(message);
            }
            em.remove(entry.getKey());
        }
    }

    @Override
    public void updateUnion(MessageUnion union) {
        em.merge(union);
    }

    @Override
    public void deleteUnion(List<Message> messages) {
        if (messages == null || messages.isEmpty()) {
            return;
        }
        List<SavedGraph> allGraphs = getSaveGraphsByDiscussion(messages.get(0).getDiscussion());
        List<SavedGraph> graphsToUpdate = new ArrayList<SavedGraph>();
        List<MessageUnion> unionsToDelete = new ArrayList<MessageUnion>();
        for (Message message : messages) {
            MessageUnion union = message.getUnion();
            if (union != null) {
                unionsToDelete.add(union);
                message.setUnion(null);
                messageDAO.update(message);
            }
        }
        for (MessageUnion union : unionsToDelete) {
            if (allGraphs != null) {
                for (SavedGraph graph : allGraphs) {
                    SavedGraph updated = removeUnitFromGraph(union.getUid(), graph);
                    if (updated != null) {
                        graphsToUpdate.add(updated);
                    }
                }
            }
            em.remove(union);
        }
        for (SavedGraph graph : graphsToUpdate) {
            em.merge(graph);
        }
    }

    // returns null if no changes
    private SavedGraph removeUnitFromGraph(int uuid, SavedGraph savedGraph) {
        JSONObject jsonObject1 = new JSONObject(savedGraph.getPositions());
        ObjectMapper mapper = new ObjectMapper();
        JavaType typeMap = mapper.getTypeFactory().constructMapType(Map.class, Integer.class, MessageFixedPoint.class);
        Map<Integer, MessageFixedPoint> positions;
        try {
            positions = mapper.readValue(jsonObject1.optString("data"), typeMap);
        }
        catch (Exception e) {
            return null;
        }
        MessageFixedPoint position = positions.get(uuid);
        if (position == null) {
            return null;
        }
        positions.remove(uuid);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("data", positions);
        savedGraph.setPositions(jsonObject.toString());
        return savedGraph;
    }

    @Override
    public void saveThesaurusUnit(ThesaurusUnit unit) {
        em.persist(unit);
    }

    @Override
    public void updateThesaurusUnit(ThesaurusUnit unit) {
        em.merge(unit);
    }

    @Override
    public void deleteThesaurusUnit(ThesaurusUnit unit) {
        em.remove(unit);
    }
}
