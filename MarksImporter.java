import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Thrown when a student ID appears more than once in the file. */
class DuplicateStudentException extends RuntimeException {
    public DuplicateStudentException(String message) {
        super(message);
    }
}

public class MarksImporter {

    private static final Logger LOGGER = Logger.getLogger(MarksImporter.class.getName());

    static void importMarks(String filename) throws IOException {
        Set<String> seen = new HashSet<>();

        // try-with-resources: the BufferedReader (and the FileReader it wraps)
        // is closed automatically when this block exits -- normally, via return,
        // or via an exception. We never call br.close() ourselves.
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            int lineNo = 0;

            while ((line = br.readLine()) != null) {
                lineNo++;
                String[] parts = line.split(",");

                // TODO 1: exactly 2 fields
                if (parts.length != 2) {
                    System.err.println("Line " + lineNo + ": expected 2 fields, found "
                            + parts.length + " -> skipped");
                    LOGGER.warning("Malformed line " + lineNo + ": " + line);
                    continue;
                }

                String id = parts[0].trim();
                String markText = parts[1].trim();

                try {
                    // TODO 2: parse + range check
                    int mark = Integer.parseInt(markText);      // may throw NumberFormatException
                    if (mark < 0 || mark > 100) {
                        throw new IllegalArgumentException("Mark out of range 0..100: " + mark);
                    }

                    // TODO 3: duplicate detection
                    if (!seen.add(id)) {
                        throw new DuplicateStudentException("Duplicate student ID: " + id);
                    }

                    // TODO 4: accepted record
                    System.out.println("Accepted: " + id + " -> " + mark);

                } catch (NumberFormatException e) {             // before IllegalArgumentException!
                    System.err.println("Invalid numeric mark for " + id + ": \""
                            + markText + "\" -> skipped");
                    LOGGER.log(Level.WARNING, "NumberFormatException for " + id, e);
                } catch (IllegalArgumentException e) {
                    System.err.println("Rejected " + id + ": " + e.getMessage() + " -> skipped");
                    LOGGER.log(Level.WARNING, "IllegalArgumentException for " + id, e);
                } catch (DuplicateStudentException e) {
                    System.err.println(e.getMessage() + " -> skipped");
                    LOGGER.log(Level.WARNING, "DuplicateStudentException", e);
                }
            }
        }
    }

    public static void main(String[] args) {

        // Send technical logs to a file so ordinary users don't see stack traces.
        try {
            FileHandler fh = new FileHandler("import.log", true); // append
            LOGGER.addHandler(fh);
            LOGGER.setUseParentHandlers(false);
        } catch (IOException e) {
            System.err.println("Could not create log file; technical logs will go to console.");
        }

        try {
            importMarks("marks.txt");
        } catch (FileNotFoundException e) {        // before IOException!
            System.out.println("Marks file not found; check the file name.");
            LOGGER.log(Level.SEVERE, "FileNotFoundException", e);
        } catch (IOException e) {
            System.out.println("Error reading the marks file. Please try again.");
            LOGGER.log(Level.SEVERE, "IOException", e);
        } finally {
            System.out.println("Import attempt finished");
        }
    }
}