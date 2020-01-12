package model;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.persistence.*;

/**
 * Notification for new seminar
 */
@Entity
@Table(name = "notifications_seminar")
public class NotificationForSeminar implements Notification {

    @Id
    @GeneratedValue
    private int id;

    @OneToOne
    private Seminar seminar;

    @OneToOne
    private User user;

    private SeminarRole role;

    @Column(nullable = false)
    private boolean notificationRead;

    @Column(nullable = false)
    private DateTime created;

    public NotificationForSeminar(Seminar seminar, User user, SeminarRole role) {
        this.seminar = seminar;
        this.user = user;
        this.role = role;
        created = new DateTime();
        notificationRead = false;
    }

    public NotificationForSeminar() {
    }

    public int getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Seminar getSeminar() {
        return seminar;
    }

    public SeminarRole getRole() {
        return role;
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
        return "Теперь вы участвуете в семинаре \"" + seminar.getName() + "\" в роли \"" +
                role.toString() + "\"";
    }

    @Override
    public String getUrl() {
        return "seminar/" + Integer.toString(seminar.getId());
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
