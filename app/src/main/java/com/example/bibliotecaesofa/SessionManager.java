package com.example.bibliotecaesofa;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

public class SessionManager {

    public static final String PREFS_NAME = "biblioteca_esofa_prefs";

    private static final String KEY_USERS = "users";
    private static final String KEY_SESSION = "session";
    private static final String DEFAULT_USER = "admin";
    private static final String DEFAULT_PASSWORD = "1234";

    private final SharedPreferences preferences;

    public SessionManager(Context context) {
        preferences = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        seedDefaultUser();
    }

    private void seedDefaultUser() {
        if (preferences.contains(KEY_USERS)) {
            return;
        }
        JSONObject users = new JSONObject();
        try {
            users.put(DEFAULT_USER, SecurityUtils.sha256(DEFAULT_PASSWORD));
        } catch (JSONException ignored) {
            // No debería ocurrir con claves simples.
        }
        preferences.edit().putString(KEY_USERS, users.toString()).apply();
    }

    public boolean login(String username, String password) {
        String user = normalize(username);
        JSONObject users = getUsers();
        if (!users.has(user)) {
            return false;
        }
        String storedHash = users.optString(user, "");
        if (!storedHash.equals(SecurityUtils.sha256(password))) {
            return false;
        }
        preferences.edit().putString(KEY_SESSION, user).apply();
        return true;
    }

    public boolean register(String username, String password) {
        String user = normalize(username);
        JSONObject users = getUsers();
        if (users.has(user)) {
            return false;
        }
        try {
            users.put(user, SecurityUtils.sha256(password));
        } catch (JSONException e) {
            return false;
        }
        preferences.edit().putString(KEY_USERS, users.toString()).apply();
        return true;
    }

    public String getCurrentUser() {
        return preferences.getString(KEY_SESSION, null);
    }

    public void logout() {
        preferences.edit().remove(KEY_SESSION).apply();
    }

    private JSONObject getUsers() {
        try {
            return new JSONObject(preferences.getString(KEY_USERS, "{}"));
        } catch (JSONException e) {
            return new JSONObject();
        }
    }

    private String normalize(String username) {
        return username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
    }
}
