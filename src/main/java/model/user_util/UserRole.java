package model.user_util;

import org.json.JSONObject;

/**
 * User role
 */
public enum UserRole {

    USER("Пользователь"),
    ADMIN("Администратор");

    private final String text;

    UserRole(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public static String[] toStringArray() {
        UserRole[] roles = values();
        String[] res = new String[roles.length];
        for (int i = 0; i < res.length; ++i) {
            res[i] = roles[i].text;
        }
        return res;
    }

    public static String toJSON() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("data", toStringArray());
        return jsonObject.toString();
    }
}