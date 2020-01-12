package model;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.persistence.*;
import java.util.List;

/**
 * Message
 */
@Entity
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue
    private int id;

    @Column(nullable = false)
    private int uid;

    @Column(nullable = false)
    private boolean first;

    @OneToOne
    private User author;

    @OneToOne
    private Discussion discussion;

    @OneToOne
    private Message previous;

    @OneToOne
    private MessageType type;

    @Column(nullable = false)
    private String color;

    @OneToOne
    private MessageUnion union;

    @Column(nullable = false, length = 20000)
    private String content;

    @Column(nullable = false, length = 10000)
    private String text;

    @Column(nullable = false)
    private DateTime created;

    private DateTime updated;

    @OneToOne
    private User updater;

    @Transient
    private List<Message> nextMessages;

    public Message() {
    }

    public boolean needsToBeTruncated() {
        return text != null && text.length() > 50;
    }

    public String getTruncatedText() {
        if (text == null || text.isEmpty()) {
            return "";
        }
        return (text.substring(0, Math.min(50, text.length())) + "..." );
    }

    public String getParsedCreated() {
        String result = null;
        if (created != null) {
            DateTimeFormatter formatter = DateTimeFormat.forPattern("dd.MM.yyyy HH:mm");
            result = formatter.print(created);
        }
        return result;
    }

    public String getParsedUpdated() {
        String result = null;
        if (updated != null) {
            DateTimeFormatter formatter = DateTimeFormat.forPattern("dd.MM.yyyy HH:mm");
            result = formatter.print(updated);
        }
        return result;
    }

    public boolean hasNextMessages() {
        return nextMessages != null && ! nextMessages.isEmpty();
    }

    public boolean canBeAnswered(SeminarParticipation participation) {
        return isDiscussionMaster(participation) || participation.getSeminarRole() != SeminarRole.SPECTATOR;
    }

    public boolean canBeUpdated(SeminarParticipation participation) {
        return isDiscussionMaster(participation) || (author.getId() == participation.getUser().getId() &&
        participation.getSeminarRole() != SeminarRole.SPECTATOR && ! hasNextMessages());
    }

    public boolean canBeDeleted(SeminarParticipation participation) {
        return ! hasNextMessages() && ( (author.getId() == participation.getUser().getId() &&
                participation.getSeminarRole() != SeminarRole.SPECTATOR) || isDiscussionMaster(participation) );
    }

    private boolean isDiscussionMaster(SeminarParticipation participation) {
        return discussion.getMaster() != null && discussion.getMaster().getId() == participation.getUser().getId();
    }

    public int getId() {
        return id;
    }

    public User getAuthor() {
        return author;
    }

    public Discussion getDiscussion() {
        return discussion;
    }

    public MessageType getType() {
        return type;
    }

    public MessageUnion getUnion() {
        return union;
    }

    public Message getPrevious() {
        return previous;
    }

    public String getContent() {
        return content;
    }

    public boolean isFirst() {
        return first;
    }

    public DateTime getCreated() {
        return created;
    }

    public DateTime getUpdated() {
        return updated;
    }

    public List<Message> getNextMessages() {
        return nextMessages;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public void setPrevious(Message previous) {
        this.previous = previous;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public void setUnion(MessageUnion union) {
        this.union = union;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setFirst(boolean first) {
        this.first = first;
    }

    public void setDiscussion(Discussion discussion) {
        this.discussion = discussion;
    }

    public void setNextMessages(List<Message> nextMessages) {
        this.nextMessages = nextMessages;
    }

    public void setCreated(DateTime created) {
        this.created = created;
    }

    public void setUpdated(DateTime updated) {
        this.updated = updated;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public User getUpdater() {
        return updater;
    }

    public void setUpdater(User updater) {
        this.updater = updater;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }
}
