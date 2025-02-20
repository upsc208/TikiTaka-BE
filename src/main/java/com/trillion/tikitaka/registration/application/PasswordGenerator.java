package com.trillion.tikitaka.registration.application;

import java.security.SecureRandom;

public class PasswordGenerator {
    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL = "@#$%^&+=!";

    private static final int LENGTH = 12;
    private static final int SPECIAL_COUNT = 2;

    private static final String ALL = UPPER + LOWER + DIGITS;
    private static final SecureRandom random = new SecureRandom();

    public static String generateRandomPassword() {
        StringBuilder password = new StringBuilder(LENGTH);

        password.append(randomChar(UPPER));
        password.append(randomChar(LOWER));
        password.append(randomChar(DIGITS));

        for (int i = 0; i < SPECIAL_COUNT; i++) {
            password.append(randomChar(SPECIAL));
        }

        for (int i = password.length(); i < LENGTH; i++) {
            password.append(randomChar(ALL));
        }

        return shuffleString(password.toString());
    }

    private static char randomChar(String source) {
        int index = random.nextInt(source.length());
        return source.charAt(index);
    }

    private static String shuffleString(String input) {
        char[] characters = input.toCharArray();
        for (int i = characters.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = characters[i];
            characters[i] = characters[j];
            characters[j] = temp;
        }
        return new String(characters);
    }
}
