package controllers.mappers;

/**
 * Message search data in JSON
 */
public class MessageSearchDataJSON {

    private String type;

    private String text;

    private int id;

    private String dateStart;

    private String dateEnd;

    public String getType() {
        return type;
    }

    public String getText() {
        return text;
    }

    public int getId() {
        return id;
    }

    public String getDateStart() {
        return dateStart;
    }

    public String getDateEnd() {
        return dateEnd;
    }
}
