package com.example.portfolioapp.network;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "PortfolioSession";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_ROLE  = "role";
    private static final String KEY_USER  = "username";

    private SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveSession(String token, String role, String username) {
        prefs.edit()
                .putString(KEY_TOKEN, token)
                .putString(KEY_ROLE, role)
                .putString(KEY_USER, username)
                .apply();
    }

    public String getToken() { return prefs.getString(KEY_TOKEN, null); }
    public String getRole()  { return prefs.getString(KEY_ROLE, "USER"); }
    public String getUser()  { return prefs.getString(KEY_USER, ""); }

    public boolean isLoggedIn() { return getToken() != null; }
    public boolean isAdmin()    { return "ADMIN".equals(getRole()); }

    public String getBearerToken() { return "Bearer " + getToken(); }

    public void logout() {
        prefs.edit().clear().apply();
    }
}