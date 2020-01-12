package controllers.mappers.util;

import java.util.ArrayList;
import java.util.List;

/**
 * JSON representation of message for graph
 */
public class MessageReprJSON {

    private int id;

    private int uid;

    private boolean isUnion;

    private String name;

    private int prevId;

    // user color
    private String color;

    private boolean first;

    private String icon;

    private String typeName;

    private int unionId;

    private List<Integer> next;

    private boolean fixed;

    private float x;

    private float y;

    public MessageReprJSON(int id, int uid, boolean isUnion, String name, int prevId, String typeName, String color, boolean first,
                           String icon, int unionId, boolean fixed, float x, float y) {
        this.id = id;
        this.uid = uid;
        this.isUnion = isUnion;
        this.name = name;
        this.prevId = prevId;
        this.typeName = typeName;
        this.color = color;
        this.first = first;
        this.icon = icon;
        this.unionId = unionId;
        next = new ArrayList<Integer>();
        this.fixed = fixed;
        this.x = x;
        this.y = y;
    }

    public void addNextMessage(int id) {
        next.add(id);
    }

    public String getName() {
        return name;
    }

    public int getPrevId() {
        return prevId;
    }

    public boolean isUnion() {
        return isUnion;
    }

    public int getId() {
        return id;
    }

    public int getUid() {
        return uid;
    }

    public String getTypeName() {
        return typeName;
    }

    public String getColor() {
        return color;
    }

    public boolean isFirst() {
        return first;
    }

    public String getIcon() {
        return icon;
    }

    public int getUnionId() {
        return unionId;
    }

    public List<Integer> getNext() {
        return next;
    }

    public boolean isFixed() {
        return fixed;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

}
