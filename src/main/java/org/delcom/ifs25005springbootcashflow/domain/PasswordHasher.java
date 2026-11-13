package org.delcom.ifs25005springbootcashflow.domain;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/** Hash dan verifikasi password dengan SHA-256 (hex). */
public final class PasswordHasher {

    private PasswordHasher() {
    }

    public static String hash(String plain) {
        return digest("SHA-256", plain);
    }

    public static boolean matches(String plain, String hash) {
        return hash(plain).equals(hash);
    }

    static String digest(String algorithm, String plain) {
        try {
            byte[] bytes = MessageDigest.getInstance(algorithm).digest(plain.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algoritma hash tidak tersedia: " + algorithm, e);
        }
    }
}
