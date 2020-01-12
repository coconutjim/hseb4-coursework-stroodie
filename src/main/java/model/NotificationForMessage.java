package model;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.persistence.*;

/**
 * Notification for new message
 */
@Entity
@Table(name = "notifications_message")
public class NotificationForMessage implements Notification {

    @Id
    @GeneratedValue
    private int id;

    @OneToOne
    private Discussion discussion;

    @OneToOne
    private Message message;

    @OneToOne
    private User user;

    @Column(nullable = false)
    private boolean notificationRead;

    @Column(nullable = false)
    private DateTime created;

    public NotificationForMessage(Discussion discussion, Message message, User user) {
        this.discussion = discussion;
        this.message = message;
        this.user = user;
        created = message.getCreated();
        notificationRead = false;
    }

    public NotificationForMessage() {
    }

    public int getId() {
        return id;
    }

    public Message getMessage() {
        return message;
    }

    public User getUser() {
        return user;
    }

    public Discussion getDiscussion() {
        return discussion;
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
        User author = message.getAuthor();
        return "Новое сообщение в дискуссии \"" + discussion.getName() + "\" от пользователя " +
                author.getFirstName() + " " + author.getLastName();
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
