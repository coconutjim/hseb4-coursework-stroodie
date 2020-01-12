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
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Message DAO implementation
 */
@Repository
public class MessageDAOImpl implements MessageDAO{

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private NotificationDAO notificationDAO;

    @Override
    public void save(Message message) {
        message.setUid(getUid());
        em.persist(message);
    }

    @Override
    public List<Message> getAll() {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Message> cq = builder.createQuery(Message.class);
        Root<Message> root = cq.from(Message.class);
        cq.select(root);
        cq.orderBy(builder.asc(root.get("id")));
        return em.createQuery(cq).getResultList();
    }

    @Override
    public Message getById(int id) {
        return em.find(Message.class, id);
    }

    @Override
    public void update(Message message) {
        em.merge(message);
    }

    @Override
    public void delete(Message message) {
        notificationDAO.deleteNotificationsForMessage(message);
        List<Message> messages = getByDiscussion(message.getDiscussion());
        List<SavedGraph> allGraphs = getSaveGraphsByDiscussion(message.getDiscussion());
        List<SavedGraph> graphsToUpdate = new ArrayList<SavedGraph>();
        MessageUnion union = message.getUnion();
        int messagesCount = 0;
        if (union != null) {
            for (Message m : messages) {
                MessageUnion un = m.getUnion();
                if (un != null && un.getId() == union.getId() && m.getId() != message.getId()) {
                    ++ messagesCount;
                }
            }
        }
        if (allGraphs != null) {
            for (SavedGraph graph : allGraphs) {
                SavedGraph updated = removeUnitFromGraph(message.getUid(), graph);
                if (updated != null) {
                    graphsToUpdate.add(updated);
                }
            }
        }
        em.remove(message);
        boolean deleteUnion = messagesCount < 2;
        if (union != null && deleteUnion) {
            for (Message m : messages) {
                MessageUnion un = m.getUnion();
                if (un != null && un.getId() == union.getId() && m.getId() != message.getId()) {
                    m.setUnion(null);
                    em.merge(m);
                }
            }
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

    private List<SavedGraph> getSaveGraphsByDiscussion(Discussion discussion) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<SavedGraph> cq = builder.createQuery(SavedGraph.class);
        Root<SavedGraph> root = cq.from(SavedGraph.class);
        cq.where(builder.equal(root.get("discussion"), discussion.getId()));
        return em.createQuery(cq).getResultList();
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
    public List<Message> getByDiscussion(Discussion discussion) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Message> cq = builder.createQuery(Message.class);
        Root<Message> root = cq.from(Message.class);
        cq.where(builder.equal(root.get("discussion"), discussion.getId()));
        cq.orderBy(builder.asc(root.get("id")));
        List<Message> messages = em.createQuery(cq).getResultList();
        if (messages != null) {
            for (Message message : messages) {
                message.setNextMessages(new ArrayList<Message>());
            }
            for (Message message : messages) {
                Message prev = message.getPrevious();
                if (prev != null) {
                    for (Message m : messages) {
                        if (m.getId() == prev.getId()) {
                            m.getNextMessages().add(message);
                        }
                    }
                }
            }
        }
        return messages;
    }

    @Override
    public Map<MessageUnion, List<Message>> getUnionsByDiscussion(Discussion discussion) {
        List<Message> messages = getByDiscussion(discussion);
        Map<MessageUnion, List<Message>> unions = new HashMap<MessageUnion, List<Message>>();
        if (messages != null) {
            for (Message message : messages) {
                MessageUnion union = message.getUnion();
                if (union != null) {
                    List<Message> ms = unions.get(union);
                    if (ms == null) {
                        ms = new ArrayList<Message>();
                        unions.put(union, ms);
                    }
                    ms.add(message);
                }
            }
            return unions.isEmpty()? null : unions;
        }
        else {
            return null;
        }
    }

    @Override
    public int getUid() {
        int max = 0;
        int uid = 0;
        List<Message> messages = getAll();
        if (messages != null && ! messages.isEmpty()) {
            for (Message message : messages) {
                uid = message.getUid();
                if (uid > max) {
                    max = uid;
                }
            }
        }
        List<MessageUnion> unions = getAllUnions();
        if (unions != null && ! unions.isEmpty()) {
            for (MessageUnion union : unions) {
                uid = union.getUid();
                if (uid > max) {
                    max = uid;
                }
            }
        }
        return max + 1;
    }

    private List<MessageUnion> getAllUnions() {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<MessageUnion> cq = builder.createQuery(MessageUnion.class);
        Root<MessageUnion> root = cq.from(MessageUnion.class);
        cq.select(root);
        cq.orderBy(builder.asc(root.get("id")));
        return em.createQuery(cq).getResultList();
    }
}
