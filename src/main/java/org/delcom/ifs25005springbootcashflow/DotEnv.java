package org.delcom.ifs25005springbootcashflow;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Memuat berkas .env ke System properties sebelum Spring Boot start. */
public final class DotEnv {

    private DotEnv() {
    }

    public static int load() {
        return load(Path.of(".env"));
    }

    /**
     * Membaca pasangan KEY=VALUE. Nilai yang sudah ada di environment OS atau
     * System property tidak ditimpa, sehingga konfigurasi server cloud tetap menang.
     *
     * @return jumlah properti yang diset
     */
    public static int load(Path file) {
        if (!Files.isRegularFile(file)) {
            return 0;
        }
        List<String> lines;
        try {
            lines = Files.readAllLines(file);
        } catch (IOException e) {
            return 0;
        }
        int count = 0;
        for (String raw : lines) {
            String line = raw.trim();
            int eq = line.indexOf('=');
            if (line.isEmpty() || line.startsWith("#") || eq <= 0) {
                continue;
            }
            String key = line.substring(0, eq).trim();
            String value = unquote(line.substring(eq + 1).trim());
            if (System.getenv(key) == null && System.getProperty(key) == null) {
                System.setProperty(key, value);
                count++;
            }
        }
        return count;
    }

    private static String unquote(String value) {
        if (value.length() >= 2) {
            char first = value.charAt(0);
            char last = value.charAt(value.length() - 1);
            if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
                return value.substring(1, value.length() - 1);
            }
        }
        return value;
    }
}
