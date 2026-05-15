package file;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileServiceTest {
    @TempDir
    Path tempDir;

    @Test
    void writesAndReadsUtf8Text() throws Exception {
        FileService service = new FileService();
        Path file = tempDir.resolve("message.txt");

        service.writeText(file, "DES Studio\nXin ch\u00E0o");

        assertEquals("DES Studio\nXin ch\u00E0o", service.readText(file));
    }

    @Test
    void writesNullContentAsEmptyFile() throws Exception {
        FileService service = new FileService();
        Path file = tempDir.resolve("empty.txt");

        service.writeText(file, null);

        assertEquals("", service.readText(file));
    }

    @Test
    void detectsSupportedTextFileExtensions() {
        FileService service = new FileService();

        assertTrue(service.isSupportedTextFile(Path.of("input.txt")));
        assertTrue(service.isSupportedTextFile(Path.of("data.csv")));
        assertTrue(service.isSupportedTextFile(Path.of("notes.md")));
        assertTrue(service.isSupportedTextFile(Path.of("app.log")));
        assertTrue(service.isSupportedTextFile(Path.of("config.json")));
        assertTrue(service.isSupportedTextFile(Path.of("layout.xml")));
        assertFalse(service.isSupportedTextFile(Path.of("report.pdf")));
        assertFalse(service.isSupportedTextFile(Path.of("document.docx")));
    }
}
