package org.delcom.ifs25005springbootcashflow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DotEnvTest {

    @TempDir
    Path tmp;

    @AfterEach
    void cleanUp() {
        for (String key : new String[]{"T_PLAIN", "T_DQ", "T_SQ", "T_DQ_OPEN", "T_SQ_OPEN", "T_EMPTY", "T_ONE", "T_PRESET"}) {
            System.clearProperty(key);
        }
    }

    @Test
    void missingFileLoadsNothing() {
        assertEquals(0, DotEnv.load(tmp.resolve("tidak-ada.env")));
    }

    @Test
    void parsesKeyValuePairsAndSkipsNoise() throws Exception {
        System.setProperty("T_PRESET", "dari-property");
        Path env = Files.writeString(tmp.resolve(".env"), String.join("\n",
                "# komentar",
                "",
                "tanpa-sama-dengan",
                "=nilai-tanpa-kunci",
                "T_PLAIN = polos ",
                "T_DQ=\"dua kutip\"",
                "T_SQ='satu kutip'",
                "T_DQ_OPEN=\"tidak ditutup",
                "T_SQ_OPEN='tidak ditutup",
                "T_EMPTY=",
                "T_ONE=x",
                "T_PRESET=ditimpa?",
                "PATH=tidak-boleh-menimpa-env-os"));
        int loaded = DotEnv.load(env);
        assertEquals(7, loaded);
        assertEquals("polos", System.getProperty("T_PLAIN"));
        assertEquals("dua kutip", System.getProperty("T_DQ"));
        assertEquals("satu kutip", System.getProperty("T_SQ"));
        assertEquals("\"tidak ditutup", System.getProperty("T_DQ_OPEN"));
        assertEquals("'tidak ditutup", System.getProperty("T_SQ_OPEN"));
        assertEquals("", System.getProperty("T_EMPTY"));
        assertEquals("x", System.getProperty("T_ONE"));
        assertEquals("dari-property", System.getProperty("T_PRESET"));
    }

    @Test
    void unreadableFileLoadsNothing() throws Exception {
        Path bad = Files.write(tmp.resolve("bad.env"), new byte[]{(byte) 0xC3, (byte) 0x28, '\n'});
        assertEquals(0, DotEnv.load(bad));
    }

    @Test
    void defaultLoadReadsDotEnvInWorkingDirectoryIfPresent() {
        assertTrue(DotEnv.load() >= 0);
    }
}
