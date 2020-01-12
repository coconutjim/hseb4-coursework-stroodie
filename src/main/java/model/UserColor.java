package model;

import java.util.*;

/**
 * User color
 */
public enum UserColor {
    COLOR1("#3F5D7D"),
    COLOR2("#279B61"),
    COLOR3("#008AB8"),
    COLOR4("#993333"),
    COLOR5("#A3E496"),
    COLOR6("#95CAE4"),
    COLOR7("#CC3333"),
    COLOR8("#FFCC33"),
    COLOR9("#FFFF7A"),
    COLOR10("#CC6699");

    private final String color;

    UserColor(String color) {
        this.color = color;
    }

    public String getColor() {
        return color;
    }

    public static List<UserColor> getRandomList() {
        List<UserColor> res = new ArrayList<UserColor>(Arrays.asList(values()));
        Collections.shuffle(res);
        return res;
    }
}
