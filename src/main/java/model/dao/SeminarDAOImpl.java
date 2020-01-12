package model.dao;

import model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

/**
 * Seminar DAO implementation
 */
@Repository
public class SeminarDAOImpl implements SeminarDAO {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private DiscussionDAO discussionDAO;

    @Autowired
    private NotificationDAO notificationDAO;

    @Override
    public void save(Seminar seminar) {
        List<SeminarParticipation> participations = seminar.getParticipations();
        List<MessageType> types = seminar.getTypes();
        List<MessageConnection> connections = seminar.getConnections();
        em.persist(seminar);
        em.flush();
        saveSeminarParticipations(participations, seminar);
        saveSeminarOntology(types, connections, seminar);
    }

    @Override
    public List<Seminar> getAll(boolean withUsers, boolean withOntology, boolean withDiscussions) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Seminar> cq = builder.createQuery(Seminar.class);
        Root<Seminar> root = cq.from(Seminar.class);
        cq.select(root);
        cq.orderBy(builder.asc(root.get("id")));
        List<Seminar> seminars = em.createQuery(cq).getResultList();

        if (seminars != null) {
            for (Seminar seminar : seminars) {
                if (withUsers) {
                    seminar.setParticipations(getSeminarParticipations(seminar));
                }
                if (withOntology) {
                    seminar.setTypes(getSeminarTypes(seminar));
                    seminar.setConnections(getSeminarConnections(seminar));
                }
                if (withDiscussions) {
                    seminar.setDiscussions(getSeminarDiscussions(seminar));
                }
            }
        }
        return seminars;
    }

    @Override
    public List<Seminar> getByUser(User user) {

        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<SeminarParticipation> cq = builder.createQuery(SeminarParticipation.class);
        Root<SeminarParticipation> root = cq.from(SeminarParticipation.class);
        cq.where(builder.equal(root.get("user"), user.getId()));
        cq.orderBy(builder.desc(root.get("id")));

        List<SeminarParticipation> participations = em.createQuery(cq).getResultList();
        List<Seminar> seminars = new ArrayList<Seminar>();
        if (participations != null) {
            for (SeminarParticipation participation : participations) {
                Seminar seminar = participation.getSeminar();
                seminar.setParticipations(getSeminarParticipations(seminar));
                seminars.add(seminar);
            }
        }
        return seminars;
    }

    @Override
    public Seminar getById(int id, boolean withUsers, boolean withOntology, boolean withDiscussions) {
        Seminar seminar =  em.find(Seminar.class, id);
        if (seminar != null) {
            if (withUsers) {
                seminar.setParticipations(getSeminarParticipations(seminar));
            }
            if (withOntology) {
                seminar.setTypes(getSeminarTypes(seminar));
                seminar.setConnections(getSeminarConnections(seminar));
            }
            if (withDiscussions) {
                seminar.setDiscussions(getSeminarDiscussions(seminar));
            }
        }
        return seminar;
    }

    @Override
    public void update(Seminar seminar) {
        // discussions here are not updated
        List<SeminarParticipation> participations = seminar.getParticipations();
        removeSeminarParticipations(seminar);
        List<MessageType> types = seminar.getTypes();
        List<MessageConnection> connections = seminar.getConnections();
        if (seminar.canChangeOntology()) {
            removeSeminarOntology(seminar);
        }
        em.merge(seminar);
        em.flush();
        saveSeminarParticipations(participations, seminar);
        if (seminar.canChangeOntology()) {
            saveSeminarOntology(types, connections, seminar);
        }
    }

    @Override
    public void delete(Seminar seminar) {
        notificationDAO.deleteNotificationsForSeminar(seminar);
        removeSeminarDiscussions(seminar);
        removeSeminarParticipations(seminar);
        removeSeminarOntology(seminar);
        em.remove(seminar);
    }

    private void saveSeminarParticipations(List<SeminarParticipation> participations, Seminar seminar) {
        if (participations != null) {
            for (SeminarParticipation participation : participations) {
                participation.setSeminar(seminar);
                em.persist(participation);
            }
        }
    }

    private List<SeminarParticipation> getSeminarParticipations(Seminar seminar) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<SeminarParticipation> cq = builder.createQuery(SeminarParticipation.class);
        Root<SeminarParticipation> root = cq.from(SeminarParticipation.class);
        cq.where(builder.equal(root.get("seminar"), seminar.getId()));
        cq.orderBy(builder.asc(root.get("id")));
        return em.createQuery(cq).getResultList();
    }

    private void removeSeminarParticipations(Seminar seminar) {
        List<SeminarParticipation> participations = getSeminarParticipations(seminar);
        if (participations != null) {
            for (SeminarParticipation participation : participations) {
                em.remove(participation);
            }
        }
    }

    private void saveSeminarOntology(List<MessageType> types, List<MessageConnection> connections, Seminar seminar) {
        if (types != null) {
            for (MessageType type : types) {
                type.setSeminar(seminar);
                em.persist(type);
                em.flush();
                if (connections != null) {
                    for (MessageConnection connection : connections) {
                        if (connection.getTypeFrom().getName().equals(type.getName())) {
                            connection.setTypeFrom(type);
                        }
                        if (connection.getTypeTo().getName().equals(type.getName())) {
                            connection.setTypeTo(type);
                        }
                    }
                }
            }
            if (connections != null) {
                for (MessageConnection connection : connections) {
                    connection.setSeminar(seminar);
                    em.persist(connection);
                }
            }
        }
    }

    private List<MessageType> getSeminarTypes(Seminar seminar) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<MessageType> cq = builder.createQuery(MessageType.class);
        Root<MessageType> root = cq.from(MessageType.class);
        cq.where(builder.equal(root.get("seminar"), seminar.getId()));
        cq.orderBy(builder.asc(root.get("id")));
        return em.createQuery(cq).getResultList();
    }

    private List<MessageConnection> getSeminarConnections(Seminar seminar) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<MessageConnection> cq = builder.createQuery(MessageConnection.class);
        Root<MessageConnection> root = cq.from(MessageConnection.class);
        cq.where(builder.equal(root.get("seminar"), seminar.getId()));
        cq.orderBy(builder.asc(root.get("id")));
        return em.createQuery(cq).getResultList();
    }

    private void removeSeminarOntology(Seminar seminar) {
        List<MessageType> types = getSeminarTypes(seminar);
        if (types != null) {
            for (MessageType type : types) {
                em.remove(type);
            }
        }
        List<MessageConnection> connections = getSeminarConnections(seminar);
        if (connections != null) {
            for (MessageConnection connection : connections) {
                em.remove(connection);
            }
        }
    }

    private List<Discussion> getSeminarDiscussions(Seminar seminar) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Discussion> cq = builder.createQuery(Discussion.class);
        Root<Discussion> root = cq.from(Discussion.class);
        cq.where(builder.equal(root.get("seminar"), seminar.getId()));
        cq.orderBy(builder.asc(root.get("id")));
        return em.createQuery(cq).getResultList();
    }

    private void removeSeminarDiscussions(Seminar seminar) {
        List<Discussion> discussions = getSeminarDiscussions(seminar);
        if (discussions != null) {
            for (Discussion discussion : discussions) {
                discussionDAO.delete(discussion);
            }
        }
    }
}
