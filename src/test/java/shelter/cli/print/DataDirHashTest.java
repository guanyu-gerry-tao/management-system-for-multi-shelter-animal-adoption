package shelter.cli.print;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link DataDirHash#compute(Path)}. Verifies deterministic hashing of
 * {@code *.csv} contents, insensitivity to file mtimes, and a clear error on a missing directory.
 */
class DataDirHashTest {

    @Test
    void sameContents_produceSameHash(@TempDir Path dir) throws Exception {
        Files.writeString(dir.resolve("a.csv"), "row1\nrow2\n");
        Files.writeString(dir.resolve("b.csv"), "rowX\n");
        String h1 = DataDirHash.compute(dir);
        String h2 = DataDirHash.compute(dir);
        assertEquals(h1, h2);
    }

    @Test
    void changedContents_produceDifferentHash(@TempDir Path dir) throws Exception {
        Files.writeString(dir.resolve("a.csv"), "row1\n");
        String h1 = DataDirHash.compute(dir);
        Files.writeString(dir.resolve("a.csv"), "row1\nrow2\n");
        String h2 = DataDirHash.compute(dir);
        assertNotEquals(h1, h2);
    }

    @Test
    void orderIndependent_mtimeDoesNotAffectHash(@TempDir Path dir) throws Exception {
        Files.writeString(dir.resolve("a.csv"), "x\n");
        Files.writeString(dir.resolve("b.csv"), "y\n");
        String h1 = DataDirHash.compute(dir);
        // Recompute after touching mtimes in reverse order
        Files.setLastModifiedTime(dir.resolve("b.csv"), FileTime.fromMillis(1));
        Files.setLastModifiedTime(dir.resolve("a.csv"), FileTime.fromMillis(2));
        String h2 = DataDirHash.compute(dir);
        assertEquals(h1, h2);
    }

    @Test
    void missingDirectory_throws() {
        assertThrows(IOException.class, () -> DataDirHash.compute(Path.of("/does/not/exist-xyz")));
    }
}
