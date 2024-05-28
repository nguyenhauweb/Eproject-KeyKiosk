package com.keykiosk.Util;

import java.util.HashSet;
import java.util.Random;

public class RandomCodeUtil {
    private static HashSet<String> generatedCodes = new HashSet<>();

    public static String generateRandomCode(int length) {
        String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        StringBuilder stringBuilder = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(chars.length());
            stringBuilder.append(chars.charAt(index));
        }
        return stringBuilder.toString();
    }

    public static String generateUniqueRandomCode() {
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code;
        Random random = new Random();

        do {
            code = new StringBuilder("#");
            for (int i = 0; i < 6; i++) {
                int index = random.nextInt(alphabet.length());
                char randomChar = alphabet.charAt(index);
                code.append(randomChar);
            }
        } while (generatedCodes.contains(code.toString()));

        generatedCodes.add(code.toString());
        return code.toString();
    }
}
