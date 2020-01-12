package controllers.mappers.util;

/**
 * Class for Type representation
 */
public class TypeRepr {

    private int id;

    private String name;

    private String iconName;

    public TypeRepr(int id, String name, String iconName) {
        this.id = id;
        this.name = name;
        this.iconName = iconName;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getIconName() {
        return iconName;
    }
}
