package model;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Seminar roles
 */
public enum SeminarRole {

    PARTICIPANT("Участник"),
    MASTER("Руководитель"),
    SPECTATOR("Наблюдатель");

    private final String text;

    SeminarRole(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }

    public static String[] toStringArray() {
        SeminarRole[] roles = values();
        String[] res = new String[roles.length];
        for (int i = 0; i < res.length; ++i) {
            res[i] = roles[i].text;
        }
        return res;
    }
    
    public static SeminarRole fromString(String str) {
        SeminarRole[] roles = values();
        for (SeminarRole role : roles) {
            if (role.toString().equals(str)) {
                return role;
            }
        }
        return null;
    }

    public static String toJSON() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("data", toStringArray());
        return jsonObject.toString();
    }
}
