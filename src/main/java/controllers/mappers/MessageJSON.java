package controllers.mappers;

/**
 * Mapper for message JSON
 */
public class MessageJSON {

    private String mode;

    private int id;

    private int discussion;

    private int type;

    private int prev;

    private String content;

    private String text;

    private boolean first;

    public String getMode() {
        return mode;
    }

    public int getId() {
        return id;
    }

    public int getDiscussion() {
        return discussion;
    }

    public int getType() {
        return type;
    }

    public int getPrev() {
        return prev;
    }

    public String getContent() {
        return content;
    }

    public String getText() {
        return text;
    }

    public boolean isFirst() {
        return first;
    }
}
