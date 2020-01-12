package model;

import controllers.mappers.util.TypeRepr;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Seminar
 */
@Entity
@Table(name = "seminars")
public class Seminar {

    @Id
    @GeneratedValue
    private int id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private DateTime created;

    @Transient
    private List<SeminarParticipation> participations;

    @Transient
    private List<MessageType> types;

    @Transient
    private List<MessageConnection> connections;

    @Transient
    private List<Discussion> discussions;

    public Seminar(String name) {
        this.name = name;
    }

    public Seminar() {
    }

    public List<List<TypeRepr>> getPossibleTypes(Message message, int numInRow) {
        List<List<TypeRepr>> result = new ArrayList<List<TypeRepr>>();
        if (types == null || types.isEmpty() || connections == null || connections.isEmpty()) {
            return result;
        }
        int lastToFill = 0;
        result.add(new ArrayList<TypeRepr>());
        if (message.isFirst()) {
            for (MessageType type : types) {
                List<TypeRepr> lastList = result.get(lastToFill);
                lastList.add(new TypeRepr(type.getId(), type.getName(), type.getIconName()));
                if (lastList.size() == numInRow) {
                    result.add(new ArrayList<TypeRepr>());
                    lastToFill++;
                }
            }
            return result;
        }
        MessageType type = message.getType();
        for (MessageConnection connection : connections) {
            if (connection.getTypeTo().getId() == type.getId()) {
                MessageType t = connection.getTypeFrom();
                List<TypeRepr> lastList = result.get(lastToFill);
                lastList.add(new TypeRepr(t.getId(), t.getName(), t.getIconName()));
                if (lastList.size() == numInRow) {
                    result.add(new ArrayList<TypeRepr>());
                    lastToFill++;
                }
            }
        }
        return result;
    }

    public boolean canChangeOntology() {
        return discussions == null || discussions.isEmpty();
    }

    public boolean hasOntology() {
        return connections != null && ! connections.isEmpty();
    }

    public boolean hasUser(User user) {
        return getUserParticipation(user) != null;
    }

    public SeminarParticipation getUserParticipation(User user) {
        if (participations != null) {
            for (SeminarParticipation participation : participations) {
                if (participation.getUser().getId() == user.getId()) {
                    return participation;
                }
            }
        }
        return null;
    }

    public MessageType getTypeById(int typeId) {
        if (types != null) {
            for (MessageType type : types) {
                if (type.getId() == typeId) {
                    return type;
                }
            }
        }
        return null;
    }

    public boolean isConnectionValid(MessageType prev, MessageType curr) {
        if (connections != null) {
            for (MessageConnection connection : connections) {
                if (connection.getTypeTo().getId() == prev.getId() && connection.getTypeFrom().getId() == curr.getId()) {
                    return true;
                }
            }
        }
        return false;
    }

    public User getMaster() {
        if (participations != null) {
            for (SeminarParticipation participation : participations) {
                if (participation.getSeminarRole() == SeminarRole.MASTER) {
                    return participation.getUser();
                }
            }
        }
        return null;
    }

    public String getUserList() {
        if (participations == null || participations.isEmpty()) {
            return "";
        }
        String result = "";
        for (SeminarParticipation participation : participations) {
            User user = participation.getUser();
            result += user.getFirstName() + " " + user.getLastName() + ", ";
        }
        return result.substring(0, result.length() - 2);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<SeminarParticipation> getParticipations() {
        return participations;
    }

    public List<MessageType> getTypes() {
        return types;
    }

    public List<MessageConnection> getConnections() {
        return connections;
    }

    public List<Discussion> getDiscussions() {
        return discussions;
    }

    public void setParticipations(List<SeminarParticipation> participations) {
        this.participations = participations;
    }

    public void setTypes(List<MessageType> types) {
        this.types = types;
    }

    public void setConnections(List<MessageConnection> connections) {
        this.connections = connections;
    }

    public void setDiscussions(List<Discussion> discussions) {
        this.discussions = discussions;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public DateTime getCreated() {
        return created;
    }

    public void setCreated(DateTime created) {
        this.created = created;
    }

    public String getParsedCreated() {
        String result = null;
        if (created != null) {
            DateTimeFormatter formatter = DateTimeFormat.forPattern("dd.MM.yyyy HH:mm");
            result = formatter.print(created);
        }
        return result;
    }
}
