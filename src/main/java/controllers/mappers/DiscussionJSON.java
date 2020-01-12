package controllers.mappers;

/**
 * Mapper for discussion JSON
 */
public class DiscussionJSON {

    private String mode;

    private int id;

    private int seminar;

    private String name;

    private String description;

    private int master;

    public String getMode() {
        return mode;
    }

    public int getId() {
        return id;
    }

    public int getSeminar() {
        return seminar;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getMaster() {
        return master;
    }
}
