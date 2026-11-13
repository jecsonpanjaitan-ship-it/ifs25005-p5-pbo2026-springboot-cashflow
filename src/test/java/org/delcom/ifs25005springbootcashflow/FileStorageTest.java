package org.delcom.ifs25005springbootcashflow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.delcom.ifs25005springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25005springbootcashflow.domain.exception.ValidationException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileStorageTest {

    @TempDir
    Path tmp;

    @Test
    void savesWithUuidNameAndKeepsExtension() throws Exception {
        FileStorage storage = new FileStorage(tmp.resolve("up").toString());
        String name = storage.save(new byte[]{1, 2, 3}, "Foto.PNG");
        assertTrue(name.endsWith(".png"));
        assertTrue(Files.exists(storage.getRoot().resolve(name)));
        assertEquals(tmp.resolve("up").toAbsolutePath().normalize(), storage.getRoot());
    }

    @Test
    void rejectsInvalidUploads() {
        FileStorage storage = new FileStorage(tmp.toString());
        assertThrows(ValidationException.class, () -> storage.save(null, "a.png"));
        assertThrows(ValidationException.class, () -> storage.save(new byte[0], "a.png"));
        assertThrows(ValidationException.class, () -> storage.save(new byte[(int) FileStorage.MAX_BYTES + 1], "a.png"));
        assertThrows(ValidationException.class, () -> storage.save(new byte[]{1}, "a.svg"));
        assertThrows(ValidationException.class, () -> storage.save(new byte[]{1}, "tanpaekstensi"));
        assertThrows(ValidationException.class, () -> storage.save(new byte[]{1}, null));
    }

    @Test
    void writeFailureBecomesUncheckedIoException() throws Exception {
        Path dir = tmp.resolve("gone");
        FileStorage storage = new FileStorage(dir.toString());
        Files.delete(dir);
        assertThrows(UncheckedIOException.class, () -> storage.save(new byte[]{1}, "a.png"));
    }

    @Test
    void constructorFailsWhenRootIsAFile() throws Exception {
        Path file = Files.writeString(tmp.resolve("afile"), "x");
        assertThrows(UncheckedIOException.class, () -> new FileStorage(file.resolve("child").toString()));
    }

    @Test
    void deleteIsSafeAndBestEffort() throws Exception {
        FileStorage storage = new FileStorage(tmp.resolve("up").toString());
        String name = storage.save(new byte[]{1}, "a.jpg");
        storage.delete(null);
        storage.delete("  ");
        storage.delete("../../etc/passwd");
        storage.delete("tidak-ada.png");
        assertTrue(Files.exists(storage.getRoot().resolve(name)));
        storage.delete(name);
        assertFalse(Files.exists(storage.getRoot().resolve(name)));

        Path dir = Files.createDirectories(storage.getRoot().resolve("sub"));
        Files.writeString(dir.resolve("x.txt"), "x");
        storage.delete("sub"); // direktori tidak kosong: gagal tapi diabaikan
        assertTrue(Files.exists(dir));
    }
}
