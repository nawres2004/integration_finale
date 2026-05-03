package org.example.utils;

import org.example.entities.User;

public class UserSession {
    private static UserSession instance;
    private User user;

    private UserSession(User user) {
        this.user = user;
    }

    public static UserSession getInstance(User user) {
        if (instance == null) {
            instance = new UserSession(user);
        }
        return instance;
    }

    public static UserSession getInstance() {
        return instance;
    }

    public User getUser() {
        return user;
    }

    public static void cleanUserSession() {
        instance = null;
    }

    @Override
    public String toString() {
        return "UserSession{" +
                "user=" + user +
                '}';
    }
}
