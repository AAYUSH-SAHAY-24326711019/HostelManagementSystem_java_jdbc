package com.hostel.util;

import java.util.regex.Pattern;

/**
 * Centralised input validation so every form fails safe instead of throwing
 * raw exceptions or letting bad data reach the database.
 */
public final class ValidationUtil {

    private static final Pattern MOBILE_PATTERN = Pattern.compile("^[6-9]\\d{9}$");
    private static final Pattern AADHAR_PATTERN = Pattern.compile("^\\d{12}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_.]{4,30}$");

    private ValidationUtil() { }

    public static boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

    public static boolean isValidMobile(String mobile) {
        return mobile != null && MOBILE_PATTERN.matcher(mobile.trim()).matches();
    }

    public static boolean isValidAadhar(String aadhar) {
        return aadhar != null && AADHAR_PATTERN.matcher(aadhar.replaceAll("\\s", "")).matches();
    }

    public static boolean isValidEmail(String email) {
        if (isEmpty(email)) return true; // email optional in many forms
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidUsername(String username) {
        return username != null && USERNAME_PATTERN.matcher(username.trim()).matches();
    }

    public static boolean isPositiveNumber(String text) {
        try {
            return Double.parseDouble(text) >= 0;
        } catch (NumberFormatException | NullPointerException e) {
            return false;
        }
    }

    public static boolean isValidAge(String text) {
        try {
            int age = Integer.parseInt(text);
            return age >= 16 && age <= 60;
        } catch (NumberFormatException | NullPointerException e) {
            return false;
        }
    }

    public static double parseDoubleSafe(String text, double fallback) {
        try {
            return Double.parseDouble(text.trim());
        } catch (Exception e) {
            return fallback;
        }
    }

    public static int parseIntSafe(String text, int fallback) {
        try {
            return Integer.parseInt(text.trim());
        } catch (Exception e) {
            return fallback;
        }
    }
}
