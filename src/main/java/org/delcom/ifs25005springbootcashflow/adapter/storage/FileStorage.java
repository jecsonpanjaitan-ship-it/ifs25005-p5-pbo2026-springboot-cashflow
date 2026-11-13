package org.delcom.ifs25005springbootcashflow.adapter.storage;

import org.delcom.ifs25005springbootcashflow.domain.exception.ValidationException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/** Menyimpan upload ke UPLOAD_DIR dengan nama UUID + ekstensi; mengembalikan path relatif. */
public class FileStorage {

    public static final long MAX_BYTES = 5L * 1024 * 1024;
    private static final Set<String> ALLOWED = Set.of("png", "jpg", "jpeg", "gif", "webp");

    private final Path root;

    public FileStorage(String uploadDir) {
        this.root = Path.of(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public Path getRoot() {
        return root;
    }

    public String save(byte[] data, String originalName) throws ValidationException {
        if (data == null || data.length == 0) {
            throw new ValidationException("File kosong");
        }
        if (data.length > MAX_BYTES) {
            throw new ValidationException("Ukuran gambar maksimal 5 MB");
        }
        String ext = extensionOf(originalName);
        if (!ALLOWED.contains(ext)) {
            throw new ValidationException("Format gambar harus PNG, JPG, GIF, atau WEBP");
        }
        String name = UUID.randomUUID() + "." + ext;
        try {
            Files.write(root.resolve(name), data);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return name;
    }

    /** Hapus file lama (best effort). Path di luar root diabaikan. */
    public void delete(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            return;
        }
        Path target = root.resolve(relativePath).normalize();
        if (!target.startsWith(root)) {
            return;
        }
        try {
            Files.deleteIfExists(target);
        } catch (IOException ignored) {
            // file lama yang gagal dihapus tidak boleh menggagalkan proses utama
        }
    }

    private static String extensionOf(String fileName) {
        String name = fileName == null ? "" : fileName;
        int dot = name.lastIndexOf('.');
        return dot < 0 ? "" : name.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
}
