package dev.gcanul.corebanking;

import java.security.SecureRandom;
import java.util.Base64;

public class KeyGenerator {
    public static void main(String[] args) {
        // Generar 32 bytes (256 bits) aleatorios de forma segura
        byte[] keyBytes = new byte[32];
        new SecureRandom().nextBytes(keyBytes);

        // Convertirlos a Base64 para que sea una cadena legible
        String secretKey = Base64.getEncoder().encodeToString(keyBytes);

        System.out.println("Tu nueva SECRET KEY es:");
        System.out.println(secretKey);
    }
}