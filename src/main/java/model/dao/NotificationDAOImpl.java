package model.dao;

import model.*;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Notification DAO implementation
 */
@Repository
public class NotificationDAOImpl implements NotificationDAO {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<Notification> getAllNotifications(User user) {
        List<Notification> notifications = new ArrayList<Notification>();

        List<NotificationForSeminar> notificationsForSeminar = getNotificationsForSeminarByUser(user);
        if (notificationsForSeminar != null) {
            notifications.addAll(notificationsForSeminar);
        }
        List<NotificationForDiscussion> notificationsForDiscussion = getNotificationsForDiscussionByUser(user);
        if (notificationsForDiscussion != null) {
            notifications.addAll(notificationsForDiscussion);
        }
        List<NotificationForMessage> notificationsForMessage = getNotificationsForMessageByUser(user);
        if (notificationsForMessage != null) {
            notifications.addAll(notificationsForMessage);
        }
        if (! notifications.isEmpty()) {
            Collections.sort(notifications, new NotificationComparator());
        }
        return notifications;
    }

    @Override
    public void createNotificationsForSeminar(Seminar seminar) {
        List<SeminarParticipation> participations = seminar.getParticipations();
        createNotificationsForSeminar(seminar, participations);

    }

    @Override
    public void createNotificationsForDiscussion(Seminar seminar, Discussion discussion) {
        List<SeminarParticipation> participations = seminar.getParticipations();
        createNotificationsForDiscussion(discussion, participations);
    }

    @Override
    public void createNotificationsForSeminar(Seminar seminar, List<SeminarParticipation> participations) {
        if (participations == null || participations.isEmpty()) {
            return;
        }
        for (SeminarParticipation participation : participations) {
            NotificationForSeminar notification = new NotificationForSeminar(seminar,
                    participation.getUser(), participation.getSeminarRole());
            em.persist(notification);
        }
    }

    @Override
    public void createNotificationsForDiscussion(Discussion discussion, List<SeminarParticipation> participations) {
        if (participations == null || participations.isEmpty()) {
            return;
        }
        for (SeminarParticipation participation : participations) {
            User user = participation.getUser();
            NotificationForDiscussion notification = new NotificationForDiscussion(discussion, user,
                    user.getId() == discussion.getMaster().getId());
            em.persist(notification);
        }
    }

    @Override
    public void createNotificationsForMessage(Seminar seminar, Message message) {
        List<SeminarParticipation> participations = seminar.getParticipations();
        if (participations == null) {
            return;
        }
        for (SeminarParticipation participation : participations) {
            User user = participation.getUser();
            if (user.getId() != message.getAuthor().getId()) {
                NotificationForMessage notification = new NotificationForMessage(message.getDiscussion(), message, user);
                em.persist(notification);
            }
        }
    }

    @Override
    public void setNotificationsForSeminarRead(Seminar seminar, User user) {
        setNotificationsRead(getNotificationsForSeminarBySeminarAndUser(seminar, user));
    }

    @Override
    public void setNotificationsForDiscussionRead(Discussion discussion, User user) {
        setNotificationsRead(getNotificationsForDiscussionByDiscussionAndUser(discussion, user));
        setNotificationsRead(getNotificationsForMessageByDiscussionAndUser(discussion, user));
    }

    @Override
    public void deleteNotificationsForSeminar(Seminar seminar) {
        List<NotificationForSeminar> notifications = getNotificationsForSeminarBySeminar(seminar);
        if (notifications != null) {
            for (NotificationForSeminar notification : notifications) {
                em.remove(notification);
            }
        }
    }

    @Override
    public void deleteNotificationsForDiscussion(Discussion discussion) {
        List<NotificationForDiscussion> notifications = getNotificationsForDiscussionByDiscussion(discussion);
        if (notifications != null) {
            for (NotificationForDiscussion notification : notifications) {
                em.remove(notification);
            }
        }
    }

    @Override
    public void deleteNotificationsForMessage(Message message) {
        List<NotificationForMessage> notifications = getNotificationsForMessageByMessage(message);
        if (notifications != null) {
            for (NotificationForMessage notification : notifications) {
                em.remove(notification);
            }
        }
    }

    private List<NotificationForSeminar> getNotificationsForSeminarBySeminar(Seminar seminar) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<NotificationForSeminar> cq = builder.createQuery(NotificationForSeminar.class);
        Root<NotificationForSeminar> root = cq.from(NotificationForSeminar.class);
        cq.where(builder.equal(root.get("seminar"), seminar.getId()));
        return em.createQuery(cq).getResultList();
    }

    private List<NotificationForDiscussion> getNotificationsForDiscussionByDiscussion(Discussion discussion) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<NotificationForDiscussion> cq = builder.createQuery(NotificationForDiscussion.class);
        Root<NotificationForDiscussion> root = cq.from(NotificationForDiscussion.class);
        cq.where(builder.equal(root.get("discussion"), discussion.getId()));
        return em.createQuery(cq).getResultList();
    }

    private List<NotificationForMessage> getNotificationsForMessageByMessage(Message message) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<NotificationForMessage> cq = builder.createQuery(NotificationForMessage.class);
        Root<NotificationForMessage> root = cq.from(NotificationForMessage.class);
        cq.where(builder.equal(root.get("message"), message.getId()));
        return em.createQuery(cq).getResultList();
    }

    private List<NotificationForSeminar> getNotificationsForSeminarByUser(User user) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<NotificationForSeminar> cq = builder.createQuery(NotificationForSeminar.class);
        Root<NotificationForSeminar> root = cq.from(NotificationForSeminar.class);
        cq.where(builder.equal(root.get("user"), user.getId()));
        return em.createQuery(cq).getResultList();
    }

    private List<NotificationForDiscussion> getNotificationsForDiscussionByUser(User user) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<NotificationForDiscussion> cq = builder.createQuery(NotificationForDiscussion.class);
        Root<NotificationForDiscussion> root = cq.from(NotificationForDiscussion.class);
        cq.where(builder.equal(root.get("user"), user.getId()));
        return em.createQuery(cq).getResultList();
    }

    private List<NotificationForMessage> getNotificationsForMessageByUser(User user) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<NotificationForMessage> cq = builder.createQuery(NotificationForMessage.class);
        Root<NotificationForMessage> root = cq.from(NotificationForMessage.class);
        cq.where(builder.equal(root.get("user"), user.getId()));
        return em.createQuery(cq).getResultList();
    }

    private List<? extends Notification> getNotificationsForSeminarBySeminarAndUser(Seminar seminar, User user) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<NotificationForSeminar> cq = builder.createQuery(NotificationForSeminar.class);
        Root<NotificationForSeminar> root = cq.from(NotificationForSeminar.class);

        Predicate[] predicates = new Predicate[2];
        predicates[0] = builder.equal(root.get("seminar"), seminar.getId());
        predicates[1] = builder.equal(root.get("user"), user.getId());
        cq.where(predicates);
        return em.createQuery(cq).getResultList();
    }

    private List<? extends Notification> getNotificationsForDiscussionByDiscussionAndUser(Discussion discussion, User user) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<NotificationForDiscussion> cq = builder.createQuery(NotificationForDiscussion.class);
        Root<NotificationForDiscussion> root = cq.from(NotificationForDiscussion.class);

        Predicate[] predicates = new Predicate[2];
        predicates[0] = builder.equal(root.get("discussion"), discussion.getId());
        predicates[1] = builder.equal(root.get("user"), user.getId());
        cq.where(predicates);
        return em.createQuery(cq).getResultList();
    }

    private List<? extends Notification> getNotificationsForMessageByDiscussionAndUser(Discussion discussion, User user) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<NotificationForMessage> cq = builder.createQuery(NotificationForMessage.class);
        Root<NotificationForMessage> root = cq.from(NotificationForMessage.class);

        Predicate[] predicates = new Predicate[2];
        predicates[0] = builder.equal(root.get("discussion"), discussion.getId());
        predicates[1] = builder.equal(root.get("user"), user.getId());
        cq.where(predicates);
        return em.createQuery(cq).getResultList();
    }

    private void setNotificationsRead(List<? extends Notification> notifications) {
        if (notifications != null) {
            for (Notification notification : notifications) {
                if (! notification.isRead()) {
                    notification.setRead(true);
                    em.merge(notification);
                }
            }
        }
    }

    class NotificationComparator implements Comparator<Notification> {
        @Override
        public int compare(Notification o1, Notification o2) {
            if (o1.getDate().isBefore(o2.getDate())) {
                return 1;
            }
            else {
                return -1;
            }
        }
    }
}
