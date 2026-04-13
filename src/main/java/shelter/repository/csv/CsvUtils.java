package shelter.repository.csv;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class providing static helper methods for reading and writing CSV data.
 * Handles quoting, escaping, splitting, and null-value encoding used uniformly
 * by all CSV-backed repository implementations in this package.
 */
public final class CsvUtils {

    /** Sentinel string used to represent a {@code null} value in CSV fields. */
    static final String NULL_MARKER = "";

    /**
     * Private constructor preventing instantiation of this utility class.
     * All methods are static and no state is held.
     */
    private CsvUtils() {
        // utility class — not instantiable
    }

    /**
     * Escapes a single field value for inclusion in a CSV row.
     * If the value is {@code null} an empty string is written; if the value contains
     * a comma, double-quote, or newline it is wrapped in double-quotes with any
     * embedded double-quotes doubled.
     *
     * @param value the field value to escape; may be {@code null}
     * @return the CSV-safe representation of {@code value}
     */
    public static String escapeCsv(String value) {
        if (value == null) {
            return NULL_MARKER;
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    /**
     * Reverses the escaping applied by {@link #escapeCsv(String)}.
     * An empty string is returned as {@code null}; quoted strings are unquoted and
     * doubled double-quotes are collapsed back to single double-quotes.
     *
     * @param value the raw CSV token to unescape; must not be {@code null}
     * @return the original string value, or {@code null} if the field was empty
     */
    public static String unescapeCsv(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        if (value.startsWith("\"") && value.endsWith("\"") && value.length() >= 2) {
            String inner = value.substring(1, value.length() - 1);
            return inner.replace("\"\"", "\"");
        }
        return value;
    }

    /**
     * Splits a CSV row into individual field tokens, correctly handling quoted fields
     * that may contain embedded commas or double-quotes.
     * The number of tokens returned equals the number of comma-separated fields in
     * {@code line}, including empty fields at either end.
     *
     * @param line a single CSV row; must not be {@code null}
     * @return an array of raw (still-escaped) field tokens
     */
    public static String[] splitCsv(String line) {
        List<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    // check for escaped quote ""
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        current.append('"');
                        i++; // skip the second quote
                    } else {
                        inQuotes = false;
                        current.append(c); // keep the closing quote for unescapeCsv
                    }
                } else {
                    current.append(c);
                }
            } else {
                if (c == '"') {
                    inQuotes = true;
                    current.append(c); // keep the opening quote for unescapeCsv
                } else if (c == ',') {
                    tokens.add(current.toString());
                    current.setLength(0);
                } else {
                    current.append(c);
                }
            }
        }
        tokens.add(current.toString());
        return tokens.toArray(new String[0]);
    }

    /**
     * Encodes a list of strings into a single semicolon-delimited field value for CSV storage.
     * An empty or {@code null} list is encoded as an empty string. Individual entries that
     * contain semicolons are not further escaped — callers must ensure entry values do not
     * contain the delimiter character.
     *
     * @param items the list to encode; may be {@code null} or empty
     * @return a semicolon-delimited string, or an empty string if the list is empty
     */
    public static String encodeList(List<String> items) {
        if (items == null || items.isEmpty()) {
            return "";
        }
        return String.join(";", items);
    }

    /**
     * Decodes a semicolon-delimited field value back into a list of strings.
     * An empty or {@code null} input returns an empty list. Each token is trimmed
     * of surrounding whitespace before being added to the result.
     *
     * @param value the encoded field value; may be {@code null} or empty
     * @return a mutable list of decoded string values, never {@code null}
     */
    public static List<String> decodeList(String value) {
        List<String> result = new ArrayList<>();
        if (value == null || value.isBlank()) {
            return result;
        }
        for (String part : value.split(";", -1)) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        return result;
    }
}
