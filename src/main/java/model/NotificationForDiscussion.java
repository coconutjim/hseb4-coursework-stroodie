package model;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.persistence.*;

/**
 * Notification for new discussion
 */
@Entity
@Table(name = "notifications_discussion")
public class NotificationForDiscussion implements Notification {

    @Id
    @GeneratedValue
    private int id;

    @OneToOne
    private Discussion discussion;

    @OneToOne
    private User user;

    @Column(nullable = false)
    private boolean master;

    @Column(nullable = false)
    private DateTime created;

    @Column(nullable = false)
    private boolean notificationRead;

    public NotificationForDiscussion(Discussion discussion, User user, boolean master) {
        this.discussion = discussion;
        this.user = user;
        this.master = master;
        created = new DateTime();
        notificationRead = false;
    }

    public NotificationForDiscussion() {

    }

    public int getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Discussion getDiscussion() {
        return discussion;
    }

    public boolean isMaster() {
        return master;
    }

    @Override
    public DateTime getDate() {
        return created;
    }

    @Override
    public String getParsedDate() {
        String result = null;
        if (created != null) {
            DateTimeFormatter formatter = DateTimeFormat.forPattern("dd.MM.yyyy HH:mm");
            result = formatter.print(created);
        }
        return result;
    }

    @Override
    public String getContent() {
        return "Теперь вы участвуете в дискуссии \"" + discussion.getName() + "\" семинара \"" +
                discussion.getSeminar().getName() + " \" в роли \"" +
                (isMaster() ? "Руководитель" : "Участник") + "\"";
    }

    @Override
    public String getUrl() {
        return "discussion/" + Integer.toString(discussion.getId());
    }

    @Override
    public void setRead(boolean read) {
        this.notificationRead = read;
    }

    @Override
    public boolean isRead() {
        return notificationRead;
    }
}