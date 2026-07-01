package com.hostel.util;

/**
 * Simple in-memory session holder for the currently logged-in user.
 * One JVM = one logged-in user at a time on the GUI client side.
 */
public final class SessionContext {

    public enum Role { ADMIN, GIRL, PARENT }

    private static Role currentRole;
    private static int currentUserId;     // admin_id / girl_id / parent_id
    private static String currentUserName;
    private static int currentGirlId;     // for PARENT role, the linked girl

    private SessionContext() { }

    public static void login(Role role, int userId, String userName, int girlId) {
        currentRole = role;
        currentUserId = userId;
        currentUserName = userName;
        currentGirlId = girlId;
    }

    public static void logout() {
        currentRole = null;
        currentUserId = -1;
        currentUserName = null;
        currentGirlId = -1;
    }

    public static boolean isLoggedIn() {
        return currentRole != null;
    }

    public static Role getRole() { return currentRole; }
    public static int getUserId() { return currentUserId; }
    public static String getUserName() { return currentUserName; }
    public static int getGirlId() { return currentGirlId; }
}
