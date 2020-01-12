package controllers.mappers;

import java.util.List;

/**
 * Mapper for seminar JSON
 */
public class SeminarJSON {

    private String mode;

    private int id;

    private String name;

    private String description;

    private List<ParticipationJSON> participations;

    private List<MessageTypeJSON> types;

    private List<MessageConnectionJSON> connections;

    public String getMode() {
        return mode;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<ParticipationJSON> getParticipations() {
        return participations;
    }

    public List<MessageTypeJSON> getTypes() {
        return types;
    }

    public List<MessageConnectionJSON> getConnections() {
        return connections;
    }
}
