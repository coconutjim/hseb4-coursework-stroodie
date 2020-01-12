package controllers.mappers;

import java.util.List;

/**
 * Mapper for union json
 */
public class UnionJSON {

    private String mode;

    private int id;

    private String name;

    private List<Integer> mesIds;

    private List<Integer> unionIds;

    public String getMode() {
        return mode;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<Integer> getMesIds() {
        return mesIds;
    }

    public List<Integer> getUnionIds() {
        return unionIds;
    }
}
