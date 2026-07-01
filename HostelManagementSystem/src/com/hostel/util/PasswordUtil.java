package com.hostel.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Handles password hashing (salted SHA-256) and temporary password generation.
 * Plain-text passwords are NEVER stored anywhere in the database.
 */
public final class PasswordUtil {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String TEMP_PWD_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789@#";

    private PasswordUtil() { }

    public static String generateSalt() {
        byte[] saltBytes = new byte[16];
        RANDOM.nextBytes(saltBytes);
        return Base64.getEncoder().encodeToString(saltBytes);
    }

    public static String hash(String plainPassword, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(Base64.getDecoder().decode(salt));
            byte[] hashed = digest.digest(plainPassword.getBytes("UTF-8"));
            // Run a second round for a bit more resistance to rainbow tables.
            for (int i = 0; i < 9999; i++) {
                hashed = digest.digest(hashed);
            }
            return Base64.getEncoder().encodeToString(hashed);
        } catch (NoSuchAlgorithmException | java.io.UnsupportedEncodingException e) {
            throw new RuntimeException("Hashing algorithm unavailable", e);
        }
    }

    public static boolean verify(String plainPassword, String salt, String expectedHash) {
        if (plainPassword == null || salt == null || expectedHash == null) return false;
        String computed = hash(plainPassword, salt);
        return slowEquals(computed, expectedHash);
    }

    /** Constant-time string comparison to reduce timing-attack risk. */
    private static boolean slowEquals(String a, String b) {
        if (a.length() != b.length()) return false;
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }

    /** Generates a random 8-character temporary password for newly created accounts. */
    public static String generateTempPassword() {
        StringBuilder sb = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            sb.append(TEMP_PWD_CHARS.charAt(RANDOM.nextInt(TEMP_PWD_CHARS.length())));
        }
        return sb.toString();
    }

    /** Basic strength check used on the "change password" forms. */
    public static boolean isStrongEnough(String pwd) {
        if (pwd == null || pwd.length() < 6) return false;
        boolean hasLetter = pwd.chars().anyMatch(Character::isLetter);
        boolean hasDigit = pwd.chars().anyMatch(Character::isDigit);
        return hasLetter && hasDigit;
    }
}
