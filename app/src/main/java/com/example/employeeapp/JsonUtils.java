package com.example.employeeapp;   // change to your package name

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Locale;

/** Helpers that work with any JSON shape, so no hard-coded field names are needed. */
public class JsonUtils {

    private static final String[] TITLE_KEYS = {
            "name", "employee_name", "employeename", "emp_name", "fullname", "full_name",
            "firstname", "first_name", "title"};
    private static final String[] SUBTITLE_KEYS = {
            "designation", "position", "role", "job", "department", "email"};

    /** Finds the first array in the JSON: either the root itself or a value inside the root object. */
    public static JSONArray findArray(String json) throws Exception {
        String trimmed = json.trim();
        if (trimmed.startsWith("[")) {
            return new JSONArray(trimmed);
        }
        JSONObject root = new JSONObject(trimmed);
        Iterator<String> keys = root.keys();
        while (keys.hasNext()) {
            Object value = root.get(keys.next());
            if (value instanceof JSONArray) {
                return (JSONArray) value;
            }
        }
        throw new Exception("No array found in the JSON response");
    }

    /** Main line shown in the list: a name-like field if present, else the first value. */
    public static String title(JSONObject o) {
        String match = valueForKeys(o, TITLE_KEYS);
        if (match != null) return match;
        return valueAtIndex(o, 0);
    }

    /** Second line shown in the list. */
    public static String subtitle(JSONObject o) {
        String match = valueForKeys(o, SUBTITLE_KEYS);
        if (match != null) return match;
        return valueAtIndex(o, 1);
    }

    /** "employee_name" or "employeeName" becomes "Employee Name". */
    public static String prettyKey(String key) {
        String spaced = key.replace('_', ' ').replaceAll("([a-z])([A-Z])", "$1 $2").trim();
        StringBuilder sb = new StringBuilder();
        for (String word : spaced.split("\\s+")) {
            if (word.isEmpty()) continue;
            sb.append(word.substring(0, 1).toUpperCase(Locale.ROOT))
                    .append(word.substring(1)).append(' ');
        }
        return sb.toString().trim();
    }

    private static String valueForKeys(JSONObject o, String[] wanted) {
        for (String w : wanted) {
            Iterator<String> it = o.keys();
            while (it.hasNext()) {
                String k = it.next();
                if (k.equalsIgnoreCase(w) && !o.isNull(k)) {
                    return String.valueOf(o.opt(k));
                }
            }
        }
        return null;
    }

    private static String valueAtIndex(JSONObject o, int index) {
        Iterator<String> it = o.keys();
        int i = 0;
        while (it.hasNext()) {
            String k = it.next();
            if (i == index) return String.valueOf(o.opt(k));
            i++;
        }
        return "";
    }
}
