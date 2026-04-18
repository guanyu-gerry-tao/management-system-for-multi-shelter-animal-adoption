package shelter.cli.print;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Computes a stable content hash over every {@code *.csv} file in a data directory.
 * Hashing ignores file ordering and mtimes — only the file name and byte contents
 * contribute. This lets the watch loop detect real data changes while ignoring
 * spurious mtime bumps from no-op writes.
 */
public final class DataDirHash {

    /** Prevents instantiation — this class exposes only a static API. */
    private DataDirHash() {
    }

    /**
     * Computes a SHA-1 hex digest over the sorted CSV files under {@code dataDir}.
     * Each file contributes its relative file name bytes, a newline, and its full content
     * to the digest in sorted-by-name order.
     *
     * @param dataDir the directory containing the CSV files; must exist and be a directory
     * @return a 40-character hex digest
     * @throws IOException if the directory cannot be read, is not a directory, or a file cannot be read
     */
    public static String compute(Path dataDir) throws IOException {
        // Guard: fail fast with a clear error when the directory does not exist
        if (!Files.isDirectory(dataDir)) {
            throw new IOException("Not a directory: " + dataDir);
        }

        // Collect every *.csv file under the directory and sort for stable hashing order
        List<Path> files = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dataDir, "*.csv")) {
            for (Path p : stream) {
                files.add(p);
            }
        }
        Collections.sort(files);

        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new IOException("SHA-1 not available", e);
        }

        // Each file contributes: filename bytes, newline separator, then full content bytes
        for (Path p : files) {
            md.update(p.getFileName().toString().getBytes());
            md.update((byte) '\n');
            md.update(Files.readAllBytes(p));
        }

        byte[] digest = md.digest();
        StringBuilder hex = new StringBuilder(digest.length * 2);
        for (byte b : digest) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }
}
